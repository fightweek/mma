package my.mma.admin.stream.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.stream.event.dto.AdminStreamFightEventDto;
import my.mma.admin.stream.event.dto.AdminStreamFightEventDto.AdminStreamFighterFightEventDto;
import my.mma.event.dto.CardStartDateTimeInfoDto;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.event.dto.StreamFightEventDto.StreamFighterDto;
import my.mma.event.dto.StreamFightEventDto.StreamFighterFightEventDto;
import my.mma.event.entity.FightEvent;
import my.mma.event.repository.FightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.stream.handler.GlobalWebSocketHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static my.mma.event.dto.StreamFighterFightEventStatus.NOW;
import static my.mma.event.dto.StreamFighterFightEventStatus.PREVIOUS;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AdminStreamEventService {

    @Value("${flask.uri}")
    private String flaskURI;
    private ScheduledFuture<?> scheduledTask;

    private final ThreadPoolTaskScheduler taskScheduler;
    private final FightEventRepository fightEventRepository;
    private final RedisUtils<StreamFightEventDto> redisUtils;
    private final RestTemplate restTemplate;
    private final GlobalWebSocketHandler socketHandler;

    public AdminStreamEventService(ThreadPoolTaskScheduler taskScheduler, FightEventRepository fightEventRepository,
                                   @Qualifier("streamFightEventRedisUtils") RedisUtils<StreamFightEventDto> redisUtils,
                                   RestTemplate restTemplate, GlobalWebSocketHandler socketHandler) {
        this.taskScheduler = taskScheduler;
        this.fightEventRepository = fightEventRepository;
        this.redisUtils = redisUtils;
        this.restTemplate = restTemplate;
        this.socketHandler = socketHandler;
    }

    public void startPolling() {
        StreamFightEventDto fightEvent = redisUtils.getData("current-event");
        fightEvent.setNow(true);
        redisUtils.setData("current-event",fightEvent);

        log.info("fight event name = {}", fightEvent.getName());
        CardStartDateTimeInfoDto dateTimeInfoDto = fightEvent.getEarlyCardDateTimeInfo() != null ?
                fightEvent.getEarlyCardDateTimeInfo() : fightEvent.getPrelimCardDateTimeInfo();
        LocalDateTime start = LocalDateTime.of(dateTimeInfoDto.getDate(), dateTimeInfoDto.getTime()).minusHours(1);

        log.info("System default timezone: {}", ZoneId.systemDefault());
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        Instant startInstant = start.atZone(zoneId).toInstant();
        Duration period = Duration.ofMinutes(5);
        scheduledTask = taskScheduler.scheduleAtFixedRate(this::requestStreamFightEventDto, startInstant, period);
    }

    private void stopPolling() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
    }

    public void requestStreamFightEventDto() {
        log.info("-----request stream fight event to FLASK-----");
        boolean isUpdated = false;
        StreamFightEventDto redisFightEvent = redisUtils.getData("current-event");
        AdminStreamFightEventDto response = restTemplate.getForObject(flaskURI + "/stream/event", AdminStreamFightEventDto.class);
        if (response == null)
            throw new CustomException(CustomErrorCode.SERVER_ERROR);
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
                            StreamFighterDto originalWinner = redisCard.getWinner();
                            redisCard.setWinner(redisCard.getLoser());
                            redisCard.setLoser(originalWinner);
                        }
                        isUpdated = true;
                        cards.set(i, redisCard);
                        break;
                    }
                }
        }
        if (isUpdated) {
            redisUtils.setData("current-event", redisFightEvent);
            boolean isCompleted = true;
            for (StreamFighterFightEventDto card : cards) {
                if (card.getResult() == null) {
                    isCompleted = false;
                    break;
                }
            }
            if (isCompleted) {
                stopPolling();
                fightEventRepository.findByName(redisFightEvent.getName()).ifPresentOrElse(
                        FightEvent::updateFightEventToCompleted,
                        () -> {
                            throw new CustomException(CustomErrorCode.SERVER_ERROR);
                        }
                );
            }
        }
        socketHandler.broadcastFightEvent(redisFightEvent);

    }

    boolean compareNames(StreamFighterFightEventDto redisCard, AdminStreamFighterFightEventDto crawledCard) {
        return crawledCard.getWinnerName().equals(redisCard.getWinner().getName()) ||
                        crawledCard.getWinnerName().equals(redisCard.getLoser().getName()) ||
                        crawledCard.getLoserName().equals(redisCard.getLoser().getName()) ||
                        crawledCard.getLoserName().equals(redisCard.getWinner().getName());
    }

}
