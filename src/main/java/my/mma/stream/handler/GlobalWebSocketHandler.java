package my.mma.stream.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fightevent.dto.StreamFightEventDto;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.stream.dto.BlockedUserIdsDto;
import my.mma.stream.dto.ChatMessageDto.ChatJoinRequest;
import my.mma.stream.dto.ChatMessageDto.ChatMessageRequest;
import my.mma.stream.dto.ChatMessageDto.ChatMessageResponse;
import my.mma.stream.dto.StreamMessageDto.ResponseMessageType;
import my.mma.stream.dto.StreamMessageDto.StreamMessageRequest;
import my.mma.stream.dto.StreamMessageDto.StreamMessageResponse;
import my.mma.stream.dto.StreamUserDto;
import my.mma.stream.dto.UserChatLog;
import my.mma.stream.dto.UserChatLog.ChatMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static my.mma.global.redis.prefix.RedisKeyPrefix.BLOCKED_USERS_PREFIX;
import static my.mma.global.redis.prefix.RedisKeyPrefix.CHAT_LOG_PREFIX;
import static my.mma.stream.dto.StreamMessageDto.ResponseMessageType.*;

@Slf4j
@RequiredArgsConstructor
public class GlobalWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final Map<String, StreamUserDto> userMap = new ConcurrentHashMap<>();
    private final RedisUtils<UserChatLog> chatLogRedisUtils;
    private final RedisUtils<BlockedUserIdsDto> blockedUsersRedisUtils;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        sessions.add(session);
        log.info("WebSocket 연결 완료, sessionId = {}", session.getId());
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                StreamMessageResponse response = StreamMessageResponse.builder()
                        .responseMessageType(CONNECTION_COUNT)
                        .connectionCount(sessions.size())
                        .build();
                ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        String sessionId = session.getId();
        log.info("payload : {}", payload);
        StreamMessageRequest request = objectMapper.readValue(payload, StreamMessageRequest.class);
        switch (request.getRequestMessageType()) {
            case JOIN -> handleJoin(sessionId, request.getChatJoinRequest());
            case TALK -> handleTalk(sessionId, request.getChatMessageRequest());
            case BLOCK -> handleBlock(sessionId, request.getUserIdToBlock());
            default -> throw new CustomException(CustomErrorCode.BAD_REQUEST_400);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        sessions.remove(session);
        userMap.remove(session.getId());
        log.info("WebSocket 연결 종료");
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                StreamMessageResponse response = StreamMessageResponse.builder()
                        .responseMessageType(ResponseMessageType.CONNECTION_COUNT)
                        .connectionCount(sessions.size())
                        .build();
                ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
        }
    }

    private void handleBlock(String sessionId, Long userIdToBlock) {
        StreamUserDto user = userMap.get(sessionId);
        BlockedUserIdsDto blockedUserIdsDto =
                blockedUsersRedisUtils.getData(BLOCKED_USERS_PREFIX.getPrefix() + user.id());
        if (blockedUserIdsDto == null) {
            blockedUserIdsDto = new BlockedUserIdsDto();
        }
        blockedUserIdsDto.getBlockedUserIds().add(userIdToBlock);
        blockedUsersRedisUtils.saveData(BLOCKED_USERS_PREFIX.getPrefix() + user.id(), blockedUserIdsDto);
    }

    private void handleTalk(String sessionId, ChatMessageRequest chatRequest) throws IOException {
        StreamUserDto user = userMap.get(sessionId);
        StreamMessageResponse response = StreamMessageResponse.builder()
                .responseMessageType(TALK)
                .chatMessageResponse(ChatMessageResponse.builder()
                        .userId(user.id())
                        .nickname(user.nickname())
                        .message(chatRequest.getMessage())
                        .point(chatRequest.getPoint())
                        .build()
                ).build();
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                /**
                 * userMap - sessionId : StreamUserDto(userId, ..)
                 * blockedUsersMap - userId : Set<Long>
                 * stream room 나갔다가 (소켓 연결 끊기고 나서 다시 들어오는 경우 : sessionId -> userMap 통해 userId 꺼내므로
                 * blockedUsersMap에 여전히 차단 목록 남음
                 */
                StreamUserDto receiver = userMap.get(ws.getId());
                if (receiver == null)
                    continue;
                BlockedUserIdsDto blocked = blockedUsersRedisUtils
                        .getData(BLOCKED_USERS_PREFIX.getPrefix() + receiver.id());
                if (blocked == null || !blocked.getBlockedUserIds().contains(user.id())) {
                    ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                }
            }
        }
        saveChatMessage(chatRequest, user);
    }

    private void handleJoin(String sessionId, ChatJoinRequest joinRequest) {
        userMap.put(sessionId, StreamUserDto.builder()
                .id(joinRequest.getUserId())
                .nickname(joinRequest.getNickname())
                .point(0)
                .build()
        );
    }

    private void saveChatMessage(ChatMessageRequest chatRequest, StreamUserDto user) {
        ChatMessage chatMessage = ChatMessage.builder().message(chatRequest.getMessage())
                .time(LocalTime.now())
                .build();
        UserChatLog userChatLog = chatLogRedisUtils.getData(CHAT_LOG_PREFIX.getPrefix() + user.id());
        if (userChatLog == null) {
            userChatLog = UserChatLog.builder()
                    .nickname(user.nickname())
                    .build();
        }
        userChatLog.addMessage(chatMessage);
        chatLogRedisUtils.saveDataWithTTL(
                CHAT_LOG_PREFIX.getPrefix() + user.id(), userChatLog, Duration.ofHours(10));
    }

    public void broadcastFightEvent(StreamFightEventDto fe) {
        try {
            for (WebSocketSession ws : sessions) {
                if (ws.isOpen()) {
                    StreamMessageResponse response = StreamMessageResponse.builder()
                            .responseMessageType(FIGHT)
                            .streamFightEvent(fe)
                            .build();
                    ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                }
            }
        } catch (IOException e) {
            log.info("io exception while broadcasting stream fight event, e = ", e);
            throw new CustomException(CustomErrorCode.SERVER_ERROR_500);
        }
    }

}
