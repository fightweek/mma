package my.mma.admin.stream.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.stream.event.dto.AdminStreamFightEventDto;
import my.mma.admin.stream.event.dto.AdminStreamFightEventDto.AdminStreamFighterFightEventDto;
import my.mma.event.dto.CardStartDateTimeInfoDto;
import my.mma.event.dto.IFighterDto;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.event.dto.StreamFightEventDto.StreamFighterDto;
import my.mma.event.dto.StreamFightEventDto.StreamFighterFightEventDto;
import my.mma.event.entity.FightEvent;
import my.mma.event.repository.FightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.stream.handler.GlobalWebSocketHandler;
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
import java.util.concurrent.ScheduledFuture;

import static my.mma.event.dto.StreamFighterFightEventStatus.NOW;
import static my.mma.event.dto.StreamFighterFightEventStatus.PREVIOUS;

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
    private final RedisUtils<StreamFightEventDto> redisUtils;
    private final RestTemplate restTemplate;
    private final GlobalWebSocketHandler socketHandler;
    private boolean eventStarted = false;

    public boolean startPolling() {
        if (scheduledTask != null) {
            log.info("There is a scheduledTask already.");
            return false;
        }
        StreamFightEventDto fightEvent = redisUtils.getData("current-event");
        // streamFightEvent status now => chat room button at HomeScreen will be activated
        fightEvent.setNow(true);
        redisUtils.updateData("current-event", fightEvent);

        log.info("fight event name = {}", fightEvent.getName());
        CardStartDateTimeInfoDto dateTimeInfoDto = fightEvent.getEarlyCardDateTimeInfo() != null ?
                fightEvent.getEarlyCardDateTimeInfo() : fightEvent.getPrelimCardDateTimeInfo();
        LocalDateTime start = LocalDateTime.of(dateTimeInfoDto.getDate(), dateTimeInfoDto.getTime()).minusMinutes(30);

        log.info("System default timezone: {}", ZoneId.systemDefault());
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        Instant startInstant = start.atZone(zoneId).toInstant();
        Duration period = Duration.ofMinutes(10);
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
        boolean isUpdated = false;
        StreamFightEventDto redisFightEvent = redisUtils.getData("current-event");
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
            log.info("=====updated stream fight event=====");
            redisUtils.updateData("current-event", redisFightEvent);
            boolean isCompleted = true;
            for (StreamFighterFightEventDto card : cards) {
                if (card.getResult() == null) {
                    isCompleted = false;
                    break;
                }
            }
            if (isCompleted) {
                stopPolling();
                log.info("=====delete current stream fight event=====");
                fightEventRepository.findByName(redisFightEvent.getName()).ifPresentOrElse(
                        FightEvent::updateFightEventToCompleted,
                        () -> {
                            throw new CustomException(CustomErrorCode.SERVER_ERROR);
                        }
                );
                redisUtils.deleteData("current-event");
            }
        }
        System.out.println(redisFightEvent.getMainCardCnt());
        System.out.println(redisFightEvent.getPrelimCardCnt());
        socketHandler.broadcastFightEvent(redisFightEvent);

    }

    boolean compareNames(StreamFighterFightEventDto redisCard, AdminStreamFighterFightEventDto crawledCard) {
        return crawledCard.getWinnerName().equals(redisCard.getWinner().getName()) ||
                crawledCard.getWinnerName().equals(redisCard.getLoser().getName()) ||
                crawledCard.getLoserName().equals(redisCard.getLoser().getName()) ||
                crawledCard.getLoserName().equals(redisCard.getWinner().getName());
    }

}
