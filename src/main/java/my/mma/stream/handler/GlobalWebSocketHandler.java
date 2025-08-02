package my.mma.stream.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.stream.chat.dto.ChatMessageDto.ChatMessageRequest;
import my.mma.stream.chat.dto.ChatMessageDto.ChatMessageResponse;
import my.mma.stream.dto.StreamMessageDto.RequestMessageType;
import my.mma.stream.dto.StreamMessageDto.ResponseMessageType;
import my.mma.stream.dto.StreamMessageDto.StreamMessageRequest;
import my.mma.stream.dto.StreamMessageDto.StreamMessageResponse;
import my.mma.stream.dto.StreamUserDto;
import my.mma.stream.dto.bet_and_vote.VoteRateDto;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static my.mma.stream.dto.StreamMessageDto.ResponseMessageType.*;

@Slf4j
@RequiredArgsConstructor
public class GlobalWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final Map<String, StreamUserDto> userMap = new ConcurrentHashMap<>();
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
        if (request.getRequestMessageType().equals(RequestMessageType.JOIN)) {
            ChatMessageRequest joinRequest = request.getChatMessageRequest();
            userMap.put(sessionId, StreamUserDto.builder()
                    .nickname(joinRequest.getMessage())
                    .point(joinRequest.getPoint()).build()
            );
        } else if (request.getRequestMessageType().equals(RequestMessageType.TALK)) {
            StreamUserDto user = userMap.get(session.getId());
            ChatMessageRequest chatRequest = request.getChatMessageRequest();
            if (user.getPoint() != chatRequest.getPoint())
                userMap.get(sessionId).setPoint(chatRequest.getPoint());
            for (WebSocketSession ws : sessions) {
                if (ws.isOpen()) {
                    StreamMessageResponse response = StreamMessageResponse.builder()
                            .responseMessageType(TALK)
                            .chatMessageResponse(ChatMessageResponse.builder()
                                    .nickname(user.getNickname())
                                    .message(chatRequest.getMessage())
                                    .point(chatRequest.getPoint())
                                    .build()
                            ).build();
                    System.out.println("response.getChatMessageResponse().getMessage() = " + response.getChatMessageResponse().getMessage());
                    ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                }
            }
        } else {
            throw new CustomException(CustomErrorCode.BAD_REQUEST_400);
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
            log.info("io exception while broadcasting stream fight event, e = ",e);
            throw new CustomException(CustomErrorCode.SERVER_ERROR);
        }
    }
}
