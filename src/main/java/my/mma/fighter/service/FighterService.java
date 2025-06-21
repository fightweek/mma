package my.mma.fighter.service;


import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.FightEventDto.FighterFightEventDto;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.repository.FighterFightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.dto.FighterDetailDto;
import my.mma.fighter.dto.FighterRankingDto;
import my.mma.fighter.dto.FighterRankingDto.RankerDto;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.instance.RankerInstance;
import my.mma.fighter.repository.FighterRepository;
import my.mma.global.s3.service.S3Service;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class FighterService {

    private final WebClient webClient;
    private final FighterRepository fighterRepository;
    private final FighterFightEventRepository fighterFightEventRepository;
    private final S3Service s3Service;

    public FighterService(FighterRepository fighterRepository, FighterFightEventRepository fighterFightEventRepository,
                          S3Service s3Service) {
        String pythonURI = "http://localhost:5000";
        this.fighterRepository = fighterRepository;
        this.webClient = WebClient.builder()
                .baseUrl(pythonURI)
                .build();
        this.fighterFightEventRepository = fighterFightEventRepository;
        this.s3Service = s3Service;
    }

    public FighterDetailDto detail(Long fighterId) {
        Fighter fighter = fighterRepository.findById(fighterId).orElseThrow(
                () -> new CustomException(CustomErrorCode.SERVER_ERROR)
        );
        Optional<List<FighterFightEvent>> fighterFightEvents = fighterFightEventRepository.findByFighter(fighter);
        List<FighterFightEventDto> fighterFightEventDtos = fighterFightEvents.map(
                ffeList -> ffeList.stream().map(FighterFightEventDto::toDto)
                        .collect(Collectors.toList())).orElse(null);
        if (fighterFightEventDtos != null)
            fighterFightEventDtos.forEach(
                    ffe -> {
                        ffe.getWinner().setImgPresignedUrl(s3Service.generateGetObjectPreSignedUrl(
                                "headshot/" + ffe.getWinner().getName().replace(' ', '-') + ".png"));
                        ffe.getLoser().setImgPresignedUrl(s3Service.generateGetObjectPreSignedUrl(
                                "headshot/" + ffe.getLoser().getName().replace(' ', '-') + ".png"));
                    }
            );
        String imgPreingedUrl = s3Service.generateGetObjectPreSignedUrl(
                "headshot/" + fighter.getName().replace(' ', '-') + ".png");
        return FighterDetailDto.toDto(fighter, fighterFightEventDtos, imgPreingedUrl);
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
                if (!rankerDto.getCategory().contains("POUND_FOR_POUND"))
                    fighter.get().updateRanking(Integer.parseInt(rankerDto.getRanking()));
                RankerInstance.getRankers().add(rankerDto);
            }
        }
    }
}
