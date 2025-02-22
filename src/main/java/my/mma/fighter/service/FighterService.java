package my.mma.fighter.service;


import lombok.extern.slf4j.Slf4j;
import my.mma.fighter.dto.FighterRankingDto;
import my.mma.fighter.dto.FighterRankingDto.RankerDto;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.instance.RankerInstance;
import my.mma.fighter.repository.FighterRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class FighterService {

    private final WebClient webClient;
    private final FighterRepository fighterRepository;

    public FighterService(FighterRepository fighterRepository) {
        String pythonURI = "http://localhost:5000";
        this.fighterRepository = fighterRepository;
        this.webClient = WebClient.builder()
                .baseUrl(pythonURI)
                .build();
    }

    @Transactional
    public void updateRanking() {
        FighterRankingDto fighterRankingDto = webClient.get()
                .uri("/ufc/fighter_ranking")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FighterRankingDto.class)
                .block();
        List<RankerDto> rankerDtos = fighterRankingDto.getRankerDtos();
        for (RankerDto rankerDto : rankerDtos) {
            Optional<Fighter> fighter = fighterRepository.findByName(rankerDto.getRankerName());
            if (fighter.isPresent()) {
                if(!rankerDto.getCategory().contains("POUND_FOR_POUND"))
                    fighter.get().updateRanking(Integer.parseInt(rankerDto.getRanking()));
                RankerInstance.getRankers().add(rankerDto);
            }
        }
    }
}
