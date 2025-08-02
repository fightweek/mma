package my.mma.admin.fighter.service;

import lombok.extern.slf4j.Slf4j;
import my.mma.admin.fighter.dto.RankersDto;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.global.redis.utils.RedisUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AdminFighterService {

    private final WebClient webClient;
    private final FighterRepository fighterRepository;
    private final RedisUtils<RankersDto> rankers;

    public AdminFighterService(FighterRepository fighterRepository, RedisUtils<RankersDto> rankers) {
        String pythonURI = "http://localhost:5000";
        this.webClient = WebClient.builder()
                .baseUrl(pythonURI)
                .build();
        this.fighterRepository = fighterRepository;
        this.rankers = rankers;
    }

    @Transactional
    public void updateRanking() {
        Optional<List<Fighter>> prevRankedFighters = fighterRepository.findFightersByRankingIsNotNull();
        prevRankedFighters.ifPresent(
                fighters -> fighters.forEach(
                        fighter -> fighter.updateRanking(null)
                )
        );
        RankersDto rankersDto = webClient.get()
                .uri("/ufc/fighter_ranking")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(RankersDto.class)
                .block();
        if (rankersDto != null) {
            rankersDto.getRankerDtos().forEach(
                    rankerDto -> {
                        Optional<Fighter> fighter = fighterRepository.findByName(rankerDto.getName());
                        if (fighter.isPresent()) {
                            if (!rankerDto.getCategory().contains("POUND_FOR_POUND"))
                                fighter.get().updateRanking(rankerDto.getRanking());
                        }
                    }
            );
            this.rankers.updateData("rankers", rankersDto);
        }
        throw new CustomException(CustomErrorCode.SERVER_ERROR,"ranker data is null");
    }

}
