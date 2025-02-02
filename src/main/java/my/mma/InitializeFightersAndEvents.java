package my.mma;

import lombok.extern.slf4j.Slf4j;
import my.mma.fighter.dto.CrawlerDto;
import my.mma.fighter.dto.CrawlerDto.EventCrawlerDto;
import my.mma.fighter.dto.CrawlerDto.FighterCrawlerDto;
import my.mma.event.entity.FightEvent;
import my.mma.fighter.entity.Fighter;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.repository.FightEventRepository;
import my.mma.fighter.repository.FighterRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
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

    private final FighterRepository fighterRepository;
    private final FightEventRepository fightEventRepository;
    private final WebClient webClient;

    public InitializeFightersAndEvents(FighterRepository fighterRepository, FightEventRepository fightEventRepository) {
        this.fighterRepository = fighterRepository;
        this.fightEventRepository = fightEventRepository;
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:5000")
                .build();
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initializeAll()  {
        Mono<CrawlerDto> responseMono = webClient.get()
                .uri("/ufc/event")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CrawlerDto.class);
        responseMono.flatMap(crawlerDto -> {
            Mono<Void> saveFighters = Mono.fromRunnable(() -> {
                List<FighterCrawlerDto> fighterDtos = crawlerDto.getFighters();
                for (FighterCrawlerDto fighterDto : fighterDtos) {
                    Optional<Fighter> findFighter = fighterRepository.findByName(fighterDto.getFighterName());
                    if (findFighter.isEmpty()) {
                        Fighter fighter = fighterDto.toEntity();
                        fighterRepository.save(fighter);
                    }
                }
            });

            Mono<Void> saveEvents = Mono.fromRunnable(() -> {
                List<EventCrawlerDto> eventDtos = crawlerDto.getEvents();
                for (EventCrawlerDto eventDto : eventDtos) {
                    FightEvent fightEvent = eventDto.toEntity();
                    for (EventCrawlerDto.Card card : eventDto.getCards()) {
                        try {
                            Fighter winner = fighterRepository.findByName(card.getWinnerName()).orElseThrow(
                                    () -> new RuntimeException("No such fighter found "+card.getWinnerName())
                            );
                            Fighter loser = fighterRepository.findByName(card.getLoserName()).orElseThrow(
                                    () -> new RuntimeException("No such fighter found "+card.getLoserName())
                            );
                            FighterFightEvent fighterFightEvent = card.toEntity(loser, winner);
                            fightEvent.addFighterFightEvent(fighterFightEvent);
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
