package my.mma.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.FightEventDto;
import my.mma.event.entity.FightEvent;
import my.mma.event.repository.FightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.s3.service.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final FightEventRepository fightEventRepository;
    private final S3Service s3Service;

    public FightEventDto getSchedule(LocalDate date) {
        Optional<FightEvent> schedule = fightEventRepository.findByEventDate(date);
        return schedule.map(
                fightEvent -> {
                    FightEventDto fightEventDto = FightEventDto.toDto(fightEvent);
                    fightEventDto.getFighterFightEvents().forEach(
                            fighterFightEventDto -> {
                                fighterFightEventDto.getWinner().setImgPresignedUrl(s3Service.generateGetObjectPreSignedUrl(
                                        "headshot/" + fighterFightEventDto.getWinner().getName().replace(' ', '-') + ".png")
                                );
                                fighterFightEventDto.getLoser().setImgPresignedUrl(s3Service.generateGetObjectPreSignedUrl(
                                        "headshot/" + fighterFightEventDto.getLoser().getName().replace(' ', '-') + ".png")
                                );
                            }
                    );
                    return fightEventDto;
                }
        ).orElse(null);
    }

}
