package my.mma.admin.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.event.dto.CrawlerDto;
import my.mma.admin.event.dto.CrawlerDto.EventCrawlerDto;
import my.mma.event.dto.CardStartDateTimeInfoDto;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.entity.property.CardStartDateTimeInfo;
import my.mma.event.repository.FightEventRepository;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.global.s3.service.S3ImgService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static my.mma.fighter.entity.FightRecord.toFightRecord;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminEventService {

    @Value("${flask.uri}")
    private String flaskURI;

    private final FighterRepository fighterRepository;
    private final FightEventRepository fightEventRepository;
    private final RestTemplate restTemplate;
    private final RedisUtils<StreamFightEventDto> redisUtils;
    private final S3ImgService s3Service;

    /**
     * 차후 경기들 및 해당 경기에 참여하는 파이터 정보 모두 반환
     */
    @Transactional
    public void saveUpcomingEvents() {
        processFetchedEventData(fetchEventData(flaskURI + "/upcoming_event"));
    }

    private CrawlerDto fetchEventData(String path) {
        return restTemplate.getForObject(path, CrawlerDto.class);
    }

    public void processFetchedEventData(CrawlerDto dto) {
        // DB에 존재하는 upcoming Events
        List<FightEvent> existingUpcomingEvents = fightEventRepository.findAllByCompletedIsWithFighterFightEvents(false);
        //
        List<FightEvent> crawledEvents = dto.getEvents().stream()
                .map(EventCrawlerDto::toEntityForEventName)
                .toList();
        // fighter 부터 삽입
        saveOrUpdateFighters(dto.getFighters());
        // upcoming -> past 상태가 된 이벤트를 업데이트
        markPastEvents(existingUpcomingEvents, crawledEvents);
        // 기존 db에 없던 새로 생긴 upcoming event 삽입
        saveNewUpcomingEvents(dto.getEvents(), existingUpcomingEvents);
    }

    private void markPastEvents(List<FightEvent> existing, List<FightEvent> crawledEvents) {
        /** DB에 존재하는 upcoming events, 새로 불러온 upcoming events 비교
         * 새로 불러온 upcoming event list에 DB에 존재하는 upcoming event가 포함되지 않으면,
         * DB의 upcoming event를 complete 상태로 update (+해당 이벤트에 포함된 fighter 전적 업데이트)
         */
        existing.stream()
                .filter(e -> crawledEvents.stream().noneMatch(fightEvent -> fightEvent.getName().equals(e.getName())))
                .forEach(e -> markEventAsCompleted(e.getName()));
    }

    private void markEventAsCompleted(String eventName) {
        log.info("mark event as completed, eventName={}", eventName);
        updateFighterAndEventFromCompletedDto(
                fetchEventData("http://flask-app:5000/ufc/prev_event?eventName=" + eventName), eventName);
    }

    private void updateFighterAndEventFromCompletedDto(CrawlerDto dto, String eventName) {
//        updateFightersRecord(dto.getFighters());
        updateCompletedFightEvent(dto.getEvents(), eventName);
    }

//    private void updateFightersRecord(List<CrawlerDto.FighterCrawlerDto> fighters) {
//        for (CrawlerDto.FighterCrawlerDto dto : fighters) {
//            fighterRepository.findByName(dto.getName()).ifPresent(fighter ->
//                    fighter.updateFightRecord(dto.getRecord().split("-")));
//        }
//    }

    private void updateCompletedFightEvent(List<EventCrawlerDto> eventDtos, String eventName) {
        FightEvent event = fightEventRepository.findByName(eventName)
                .orElseThrow(() -> new RuntimeException("No such fightEvent"));

        eventDtos.forEach(eventDto -> {
            for (EventCrawlerDto.Card card : eventDto.getCards()) {
                for (FighterFightEvent match : event.getFighterFightEvents()) {
                    if (card.getWinnerName().equals(match.getWinner().getName()) ||
                            card.getWinnerName().equals(match.getLoser().getName())) {
                        match.updateFightResult(card.buildFightResult());
                        match.updateDrawAndNc(card.isDraw(),card.isNc());
                        if(!card.getWinnerName().equals(match.getWinner().getName())){
                            match.swapWinnerAndLoser();
                        }
                    }
                }
            }
        });
    }

    private void saveOrUpdateFighters(List<CrawlerDto.FighterCrawlerDto> fighterDtos) {
        for (CrawlerDto.FighterCrawlerDto dto : fighterDtos) {
            fighterRepository.findByName(dto.getName()).ifPresentOrElse(
                    existing -> {
                        if (!existing.getFightRecord().equals(toFightRecord(dto.getRecord().split("-")))) {
                            existing.updateFightRecord(dto.getRecord().split("-"));
                        }
                    },
                    () -> fighterRepository.save(dto.toEntity())
            );
        }
        System.out.println("=======save fighters completed========");
    }

    private void saveNewUpcomingEvents(List<EventCrawlerDto> eventDtos, List<FightEvent> existingEvents) {
        for (int i = 0; i < eventDtos.size(); i++) {
            EventCrawlerDto dto = eventDtos.get(i);
            FightEvent newEvent = dto.toEntityUpcomingEvent();
            // 1. DB의 upcoming event에 crawling으로 불러온 upcoming event가 포함되지 않는 경우 => 이는 새로 생긴 upcoming event
            // 2. event name 이 같더라도, event 내부의 fighter fight event 내용 다를 경우 => 이는 기존 event 내용 변경된 케이스
            FightEvent existingEvent = existingEvents.stream()
                    .filter((existing) -> existing.getEventDate().equals(newEvent.getEventDate()))
                    .findFirst()
                    .orElse(null);
            if (existingEvent == null) {
                saveUpcomingEvents(dto, newEvent);
            } else {
                boolean isChanged = isEventContentDifferent(dto, existingEvent, newEvent);
                System.out.println("isChanged="+isChanged);
                if (isChanged) {
                    fightEventRepository.delete(existingEvent);
                    saveUpcomingEvents(dto, newEvent);
                }
            }
            if (i == 0)
                saveStreamFightEvent(newEvent);
        }
    }

    private boolean isEventContentDifferent(EventCrawlerDto dto, FightEvent existingEvent, FightEvent newEvent) {
        List<FighterFightEvent> existingCards = existingEvent.getFighterFightEvents();
        dto.getCards().forEach(
                card -> {
                    Fighter winner = fighterRepository.findByName(card.getWinnerName())
                            .orElseThrow(() -> new RuntimeException("No such fighter: " + card.getWinnerName()));
                    Fighter loser = fighterRepository.findByName(card.getLoserName())
                            .orElseThrow(() -> new RuntimeException("No such fighter: " + card.getLoserName()));
                    newEvent.addFighterFightEvent(card.toEntity(winner, loser));
                }
        );
        List<FighterFightEvent> crawledEvenCards = newEvent.getFighterFightEvents();

        if (existingCards.size() != crawledEvenCards.size()) return true;

        for (int i = 0; i < existingCards.size(); i++) {
            FighterFightEvent a = existingCards.get(i);
            FighterFightEvent b = crawledEvenCards.get(i);

            if (!a.getWinner().getName().equals(b.getWinner().getName()) ||
                    !a.getLoser().getName().equals(b.getLoser().getName())) {
                return true;
            }
        }
        return false;
    }

    private void saveUpcomingEvents(EventCrawlerDto dto, FightEvent event) {
        for (EventCrawlerDto.Card card : dto.getCards()) {
            try {
                Fighter winner = fighterRepository.findByName(card.getWinnerName())
                        .orElseThrow(() -> new RuntimeException("No such fighter: " + card.getWinnerName()));
                Fighter loser = fighterRepository.findByName(card.getLoserName())
                        .orElseThrow(() -> new RuntimeException("No such fighter: " + card.getLoserName()));
                FighterFightEvent fight = card.toEntity(winner, loser);
                event.addFighterFightEvent(fight);
            } catch (Exception e) {
                log.warn("Error linking fighters to fightEvent: {}", e.getMessage());
            }
        }
        fightEventRepository.save(event);
    }

    public void saveStreamFightEvent(FightEvent fightEvent) {
        StreamFightEventDto streamFightEvent = StreamFightEventDto.toDto(fightEvent);
        streamFightEvent.getFighterFightEvents().forEach(
                ffe -> {
                    ffe.setWinnerVoteRate(0);
                    ffe.setLoserVoteRate(0);
                    ffe.getWinner().setHeadshotUrl(s3Service.generateImgUrl(
                            "headshot/" + ffe.getWinner().getName().replace(' ', '-') + ".png", 350)
                    );
                    ffe.getLoser().setHeadshotUrl(s3Service.generateImgUrl(
                            "headshot/" + ffe.getLoser().getName().replace(' ', '-') + ".png", 350)
                    );
                    ffe.getWinner().setBodyUrl(s3Service.generateImgUrl(
                            "body/" + ffe.getWinner().getName().replace(' ', '-') + ".png", 350)
                    );
                    ffe.getLoser().setBodyUrl(s3Service.generateImgUrl(
                            "body/" + ffe.getLoser().getName().replace(' ', '-') + ".png", 350)
                    );
                }
        );
        redisUtils.saveData("current-event", streamFightEvent);
    }
}
