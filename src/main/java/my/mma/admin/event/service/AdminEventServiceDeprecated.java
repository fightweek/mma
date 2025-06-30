//package my.mma.admin.event.service;
//
//import lombok.extern.slf4j.Slf4j;
//import my.mma.admin.event.dto.CrawlerDto;
//import my.mma.admin.event.dto.CrawlerDto.EventCrawlerDto;
//import my.mma.admin.event.dto.CrawlerDto.FighterCrawlerDto;
//import my.mma.event.entity.FightEvent;
//import my.mma.event.entity.FighterFightEvent;
//import my.mma.event.repository.FightEventRepository;
//import my.mma.fighter.entity.Fighter;
//import my.mma.fighter.repository.FighterRepository;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//import java.util.Optional;
//
//import static my.mma.admin.event.dto.CrawlerDto.EventCrawlerDto.Card;
//import static my.mma.fighter.entity.FightRecord.toFightRecord;
//
//@Transactional(readOnly = true)
//@Service
//@Slf4j
//public class AdminEventService {
//
//    private final FighterRepository fighterRepository;
//    private final FightEventRepository fightEventRepository;
//    private final WebClient webClient;
//
//    public AdminEventService(FighterRepository fighterRepository, FightEventRepository fightEventRepository) {
//        String pythonURI = "http://localhost:5000";
//        log.info("Python Server URI: {}", pythonURI);
//        this.fighterRepository = fighterRepository;
//        this.fightEventRepository = fightEventRepository;
//        this.webClient = WebClient.builder()
//                .baseUrl(pythonURI)
//                .build();
//    }
//
//    @Transactional
//    public void saveUpcomingEvent(){
//        Mono<CrawlerDto> responseMono = webClient.get()
//                .uri("/ufc/upcoming_event")
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .bodyToMono(CrawlerDto.class);
//        saveFighterAndEvent(responseMono);
//    }
//
//    @Transactional
//    public void savePrevEvent(String eventName){
//
//    }
//
//    private void saveFighterAndEvent(Mono<CrawlerDto> responseMono) {
//        List<FightEvent> upComingEventsInDB = fightEventRepository.findAllByCompleted(false);
//        responseMono.flatMap(crawlerDto -> {
//            Mono<Void> saveFightersMono = Mono.fromRunnable(() -> {
//                List<FighterCrawlerDto> fighterDtos = crawlerDto.getFighters();
//                // 차후 이벤트에 참여하는 파이터들 중 정보가 없는 fighter 정보는 저장 (전적이 수정되었다면, 해당 fighter 전적 수정)
//                saveFighters(fighterDtos);
//            });
//            Mono<Void> saveEventsMono = Mono.fromRunnable(() -> {
//                List<EventCrawlerDto> upComingEventDtos = crawlerDto.getEvents();
//                List<FightEvent> upcomingEvents = upComingEventDtos.stream().map(EventCrawlerDto::toEntityPrevEvent).toList();
//                for (FightEvent upComingEventInDB : upComingEventsInDB) {
//                    /**
//                     * 기존 upComingEvent를 Completed 상태로 변환 (현재 퍼온 upComingEvents에 기존 upComingEvent가 없을 때)
//                     * 따라서 /ufc/prev_event 경로로 해당 prev 상태로 변한 이벤트 정보 요청
//                     */
//                    if(!upcomingEvents.contains(upComingEventInDB)){
//                        String eventName = upComingEventInDB.getEventName();
//                        Mono<CrawlerDto> toCompletedEventMono = webClient.get()
//                                .uri("/ufc/prev_event?eventName="+eventName)
//                                .accept(MediaType.APPLICATION_JSON)
//                                .retrieve()
//                                .bodyToMono(CrawlerDto.class);
//                        toCompletedEventMono.flatMap(toCompletedEvent -> {
//                            Mono<Void> updateFighterMono = Mono.fromRunnable(() -> {
//                                        toCompletedEvent.getFighters().forEach(
//                                                fighterCrawlerDto -> {
//                                                    Fighter fighter = fighterCrawlerDto.toEntity();
//                                                    fighter.updateFightRecord(fighterCrawlerDto.getRecord().split("-"));
//                                                }
//                                        );
//                                    });
//                            Mono<Void> updateFightEventMono = Mono.fromRunnable(() -> {
//                                // 실제로는 하나의 이벤트(getEvents size=1)지만, dto 형식을 맞추기 위해 이렇게 설정
//                                toCompletedEvent.getEvents().forEach(
//                                        eventCrawlerDto -> {
//                                            FightEvent findFightEvent = fightEventRepository.findByEventName(eventName).orElseThrow(
//                                                    () -> new RuntimeException("no such fightEvent")
//                                            );
//                                            for (Card card : eventCrawlerDto.getCards()) {
//                                                for (FighterFightEvent fighterFightEvent : findFightEvent.getFighterFightEvents()) {
//                                                    if (card.getWinnerName().equals(fighterFightEvent.getWinner().getName()) ||
//                                                            card.getWinnerName().equals(fighterFightEvent.getLoser().getName())) {
//                                                        fighterFightEvent.updateFighterFightEvent(card.buildFightResult());
//                                                    }
//                                                }
//                                            }
//                                        }
//                                );
//                            });
//                            return updateFighterMono.then(updateFightEventMono);
//                        }).subscribe();
//                    }
//                }
//                for (EventCrawlerDto eventDto : upComingEventDtos) {
//                    FightEvent fightEvent = eventDto.toEntityUpcomingEvent(); // 크롤링한 Upcoming fightEvent
//                    // 기존 upComingEvents에 현재 퍼온 upComingEvent가 없다면, 이는 새로운 upComingEvent
//                    if(!upComingEventsInDB.contains(fightEvent)){
//                        saveUpcomingEvent(eventDto, fightEvent);
//                    }
//                }
//            });
//            return saveFightersMono.then(saveEventsMono); // Fighter 저장 후 Event 저장
//        }).subscribe();
//    }
//
//    private void saveFighters(List<FighterCrawlerDto> fighterDtos) {
//        for (FighterCrawlerDto fighterDto : fighterDtos) {
//            Optional<Fighter> findFighter = fighterRepository.findByName(fighterDto.getFighterName());
//            if (findFighter.isEmpty()) {
//                Fighter fighter = fighterDto.toEntity();
//                fighterRepository.save(fighter);
//            }
//            else {
//                // 만약 퍼온 fighter가 존재하지만, 기록이 수정되었을 때
//                if(findFighter.get().getFightRecord() != toFightRecord(fighterDto.getRecord().split("-")))
//                {
//                    findFighter.get().updateFightRecord(fighterDto.getRecord().split("-"));
//                }
//            }
//        }
//    }
//
//    private void saveUpcomingEvent(EventCrawlerDto eventDto, FightEvent fightEvent) {
//        for (Card card : eventDto.getCards()) {
//            try {
//                Fighter winner = fighterRepository.findByName(card.getWinnerName()).orElseThrow(
//                        () -> new RuntimeException("No such fighter found "+card.getWinnerName())
//                );
//                Fighter loser = fighterRepository.findByName(card.getLoserName()).orElseThrow(
//                        () -> new RuntimeException("No such fighter found "+card.getLoserName())
//                );
//                FighterFightEvent fighterFightEvent = card.toEntityUpcomingEvent(winner, loser);
//                fighterFightEvent.addFightEvent(fightEvent);
//            } catch (RuntimeException e) {
//                log.info("error = ", e);
//            }
//        }
//        fightEventRepository.save(fightEvent);
//    }
//}