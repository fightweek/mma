package my.mma.admin.fighter.service;

import lombok.extern.slf4j.Slf4j;
import my.mma.admin.fighter.dto.ChosenGameFighterNamesDto;
import my.mma.admin.fighter.dto.RankersDto;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.global.s3.service.S3ImgService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AdminFighterService {

    private final WebClient webClient;
    private final FighterRepository fighterRepository;
    private final RedisUtils<RankersDto> rankerRedisUtils;
    private final RedisUtils<ChosenGameFighterNamesDto> adminChosenFightersRedisUtils;
    private final S3ImgService s3ImgService;

    public AdminFighterService(FighterRepository fighterRepository, RedisUtils<RankersDto> rankers,
                               RedisUtils<ChosenGameFighterNamesDto> chosenFighters, S3ImgService s3ImgService) {
        String pythonURI = "http://flask-app:5000";
        this.webClient = WebClient.builder()
                .baseUrl(pythonURI)
                .build();
        this.fighterRepository = fighterRepository;
        this.rankerRedisUtils = rankers;
        this.adminChosenFightersRedisUtils = chosenFighters;
        this.s3ImgService = s3ImgService;
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
            this.rankerRedisUtils.updateData("rankers", rankersDto);
        } else
            throw new CustomException(CustomErrorCode.SERVER_ERROR, "ranker data is null");
    }

    @Transactional
    public void saveAdminChosenFighters(List<String> fighterNames) {
        ChosenGameFighterNamesDto chosenFighters = adminChosenFightersRedisUtils.getData("chosenFighters");
        chosenFighters.getNames().addAll(fighterNames);
        adminChosenFightersRedisUtils.updateData("chosenFighters", chosenFighters);
    }

    public void updateImage(String fighterName) {
        try {
            webClient.put()
                    .uri("/ufc/fighter_image")
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("fighterName", fighterName))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.SERVER_ERROR, "error while updating fighter image, e=" + e.getMessage());
        }
    }
}
