package my.mma;

import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.BasicCrawlerDto;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.repository.FightEventRepository;
import my.mma.event.service.EventService;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.user.entity.User;
import my.mma.security.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Transactional(readOnly = true)
public class InitializeFightersAndEvents {

    private final EventService eventService;
    private final WebClient webClient;
    private final FighterRepository fighterRepository;
    private final FightEventRepository fightEventRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public InitializeFightersAndEvents(EventService eventService, FighterRepository fighterRepository,
                                       FightEventRepository fightEventRepository, UserRepository userRepository,
                                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        String pythonURI = "http://localhost:5000";
//        String pythonURI = "http://host.docker.internal:5000";
        log.info("Python Server URI: {}", pythonURI);
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.eventService = eventService;
        this.fighterRepository = fighterRepository;
        this.fightEventRepository  = fightEventRepository;
        this.webClient = WebClient.builder()
                .baseUrl(pythonURI)
                .build();
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initializeAll()  {
        User user = User.builder()
                .email("jht1234@naver.com")
                .password(bCryptPasswordEncoder.encode("pwd123"))
                .nickname("진현택")
                .role("ROLE_USER")
                .build();
        userRepository.save(user);
//        Mono<BasicCrawlerDto> responseMono = webClient.get()
//                .uri("/ufc/prev_events")
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .bodyToMono(BasicCrawlerDto.class);
//        saveFighterAndEvent(responseMono, true);
    }

    private void saveFighterAndEvent(Mono<BasicCrawlerDto> responseMono, boolean isCompleted) {
        responseMono.flatMap(crawlerDto -> {
            Mono<Void> saveFighters = Mono.fromRunnable(() -> {
                List<BasicCrawlerDto.FighterCrawlerDto> fighterDtos = crawlerDto.getFighters();
                for (BasicCrawlerDto.FighterCrawlerDto fighterDto : fighterDtos) {
                    Optional<Fighter> findFighter = fighterRepository.findByName(fighterDto.getFighterName());
                    if (findFighter.isEmpty()) {
                        Fighter fighter = fighterDto.toEntity();
                        fighterRepository.save(fighter);
                    }
                }
            });
            Mono<Void> saveEvents = Mono.fromRunnable(() -> {
                List<BasicCrawlerDto.EventCrawlerDto> eventDtos = crawlerDto.getEvents();
                for (BasicCrawlerDto.EventCrawlerDto eventDto : eventDtos) {
                    FightEvent fightEvent = isCompleted ? eventDto.toEntityPrevEvent() : eventDto.toEntityUpcomingEvent();
                    for (BasicCrawlerDto.EventCrawlerDto.Card card : eventDto.getCards()) {
                        try {
                            Fighter winner = fighterRepository.findByName(card.getWinnerName()).orElseThrow(
                                    () -> new RuntimeException("No such fighter found "+card.getWinnerName())
                            );
                            Fighter loser = fighterRepository.findByName(card.getLoserName()).orElseThrow(
                                    () -> new RuntimeException("No such fighter found "+card.getLoserName())
                            );
                            FighterFightEvent fighterFightEvent = isCompleted ? card.toEntityPrevEvent(winner,loser) : card.toEntityUpcomingEvent(winner, loser);
                            fighterFightEvent.addFightEvent(fightEvent);
                        } catch (RuntimeException e) {
                            log.info("error = ", e);
                        }
                    }
                    fightEventRepository.save(fightEvent);
                }
            });
            return saveFighters.then(saveEvents); // Fighter 저장 후 Event 저장
        }).subscribe();
    }

}
