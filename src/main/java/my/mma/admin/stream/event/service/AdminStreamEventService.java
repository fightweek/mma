package my.mma.admin.stream.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.stream.event.dto.AdminStreamFightEventDto;
import my.mma.admin.stream.event.dto.AdminStreamFightEventDto.AdminStreamFighterFightEventDto;
import my.mma.fightevent.dto.CardStartDateTimeInfoDto;
import my.mma.fightevent.dto.StreamFightEventDto;
import my.mma.fightevent.dto.StreamFightEventDto.FighterFightEventCardFighterDto;
import my.mma.fightevent.dto.StreamFightEventDto.StreamFighterFightEventDto;
import my.mma.fightevent.entity.FightEvent;
import my.mma.fightevent.repository.FightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.global.s3.service.S3JsonFileService;
import my.mma.stream.dto.UserChatLog;
import my.mma.stream.handler.GlobalWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static my.mma.fightevent.dto.StreamFighterFightEventStatus.NOW;
import static my.mma.fightevent.dto.StreamFighterFightEventStatus.PREVIOUS;
import static my.mma.global.redis.prefix.RedisKeyPrefix.CHAT_LOG_PREFIX;

/**
 * 스케줄러가 백그라운드 스레드 풀 내 워커 스레드에서 작업을 실행
 * 즉, 스케줄링된 메서드는 이미 별도의 스레드에서 실행됨 (메인 스레드와 분리)
 * 따라서 스케줄링된 메서드 내 작업은 기본적으로 비동기적임
 */

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminStreamEventService {

    @Value("${flask.uri}")
    private String flaskURI;
    private ScheduledFuture<?> scheduledTask;

    private final ThreadPoolTaskScheduler taskScheduler;
    private final FightEventRepository fightEventRepository;
    private final StreamBetUpdateService streamBetUpdateService;
    private final RedisUtils<StreamFightEventDto> streamFightEventRedisUtils;
    private final RedisUtils<UserChatLog> userChatLogRedisUtils;
    private final RestTemplate restTemplate;
    private final GlobalWebSocketHandler socketHandler;
    private final S3JsonFileService s3JsonFileService;
    private final ObjectMapper objectMapper;
    private boolean eventStarted = false;

    public boolean startPolling() {
        if (scheduledTask != null) {
            log.info("There is a scheduledTask already.");
            return false;
        }
        StreamFightEventDto fightEvent = streamFightEventRedisUtils.getData("current-event");
        // streamFightEvent status now => chat room button at HomeScreen will be activated
        fightEvent.setNow(true);
        streamFightEventRedisUtils.updateData("current-event", fightEvent);

        log.info("fight event name = {}", fightEvent.getName());
        CardStartDateTimeInfoDto dateTimeInfoDto = fightEvent.getEarlyCardDateTimeInfo() != null ?
                fightEvent.getEarlyCardDateTimeInfo() : fightEvent.getPrelimCardDateTimeInfo();
        LocalDateTime start = LocalDateTime.of(dateTimeInfoDto.date(), dateTimeInfoDto.time()).minusMinutes(30);

        log.info("System default timezone: {}", ZoneId.systemDefault());
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        Instant startInstant = start.atZone(zoneId).toInstant();
        Duration period = Duration.ofMinutes(3);
        scheduledTask = taskScheduler.scheduleAtFixedRate(this::requestStreamFightEventDto, startInstant, period);
        return true;
    }

    private void stopPolling() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            eventStarted = false;
        }
    }

    public void requestStreamFightEventDto() {
        log.info("-----request stream fight event to FLASK-----");
        saveChatLogsToS3();
        boolean isUpdated = false;
        StreamFightEventDto redisFightEvent = streamFightEventRedisUtils.getData("current-event");
        AdminStreamFightEventDto response = restTemplate.getForObject(flaskURI + "/stream/event?eventName="
                + redisFightEvent.getName(), AdminStreamFightEventDto.class);
        if (response == null) {
            log.info("event not started yet");
            return;
        } else {
            if (!eventStarted) {
                eventStarted = true;
                redisFightEvent.getFighterFightEvents().get(redisFightEvent.getFighterFightEvents().size() - 1).setStatus(NOW);
                log.info("첫 번째 카드에 NOW 상태 설정됨");
                streamFightEventRedisUtils.updateData("current-event", redisFightEvent);
            }
        }
        System.out.println(response);
        List<StreamFighterFightEventDto> cards = redisFightEvent.getFighterFightEvents();

        for (int i = cards.size() - 1; i >= 0; i--) {
            StreamFighterFightEventDto redisCard = cards.get(i);
            if (isUpdated) {
                redisCard.setStatus(NOW);
                cards.set(i, redisCard);
                break;
            }
            if (redisCard.getResult() == null)
                for (AdminStreamFighterFightEventDto crawledCard : response.getStreamFighterFightEvents()) {
                    /**
                     * 현재 경기 결과 업데이트 및 다음 경기를 현재경기로 설정하는 조건
                     * 1. 현재 카드(Redis에 저장된)경기의 상태 = NOW 2. 크롤링한 카드의 경기 결과 존재 3. 이름 비교
                     * 만약, 레디스에 저장된 선수 이름 순서가 크롤링으로 가져온 승자, 패자 이름순과 다를 때 -> SWAP
                     */
                    if (redisCard.getStatus().equals(NOW) && crawledCard.getResult() != null &&
                            compareNames(redisCard, crawledCard)) {
                        redisCard.setResult(crawledCard.getResult());
                        redisCard.setStatus(PREVIOUS);
                        if (!redisCard.getWinner().getName().equals(crawledCard.getWinnerName())) {
                            FighterFightEventCardFighterDto originalWinner = redisCard.getWinner();
                            redisCard.setWinner(redisCard.getLoser());
                            redisCard.setLoser(originalWinner);
                        }
                        streamBetUpdateService.updateBetData(redisCard);
                        isUpdated = true;
                        cards.set(i, redisCard);
                        break;
                    }
                }
        }
        if (isUpdated) {
            log.info("=====updated stream fight event=====");
            streamFightEventRedisUtils.updateData("current-event", redisFightEvent);
            boolean isCompleted = true;
            for (StreamFighterFightEventDto card : cards) {
                if (card.getResult() == null) {
                    isCompleted = false;
                    break;
                }
            }
            // 이벤트의 모든 카드에 대한 경기가 끝난 케이스
            if (isCompleted) {
                stopPolling();
                log.info("=====delete current stream fight event=====");
                fightEventRepository.findByName(redisFightEvent.getName()).ifPresentOrElse(
                        FightEvent::updateFightEventToCompleted,
                        () -> {
                            throw new CustomException(CustomErrorCode.SERVER_ERROR_500);
                        }
                );
                streamFightEventRedisUtils.deleteData("current-event");
//                saveChatLogsToS3();
            }
        }
        // voteRate의 실시간 변동 때문에 이벤트 업데이트 여부와 상관 없이 주기적으로 broadCast 해야 됨
        socketHandler.broadcastFightEvent(redisFightEvent);
        saveChatLogsToS3();
    }

    private void saveChatLogsToS3() {
        LocalDate today = LocalDate.now();
        String todayString = today.getYear() + "/" + today.getMonthValue() + "/" + today.getDayOfMonth() + "/";
        Map<String, UserChatLog> chatLogs = userChatLogRedisUtils.getAllWithKeyFromPrefix(CHAT_LOG_PREFIX.getPrefix());
        if (!chatLogs.isEmpty())
            chatLogs.forEach((chatLogPrefixWithUserId, userChatLog) -> {
                try {
                    String strUserChatLog = objectMapper.writeValueAsString(userChatLog);
                    s3JsonFileService.uploadChatLog(
                            todayString + chatLogPrefixWithUserId + ".json", strUserChatLog);
                } catch (JsonProcessingException e) {
                    throw new CustomException(CustomErrorCode.SERVER_ERROR_500, "json parse error white save chat log " + chatLogPrefixWithUserId);
                }
            });
    }

    boolean compareNames(StreamFighterFightEventDto redisCard, AdminStreamFighterFightEventDto crawledCard) {
        return crawledCard.getWinnerName().equals(redisCard.getWinner().getName()) ||
                crawledCard.getWinnerName().equals(redisCard.getLoser().getName()) ||
                crawledCard.getLoserName().equals(redisCard.getLoser().getName()) ||
                crawledCard.getLoserName().equals(redisCard.getWinner().getName());
    }

}
