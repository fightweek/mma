package my.mma.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.FightEventDto;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.event.entity.FightEvent;
import my.mma.event.repository.FightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.s3.service.S3Service;
import my.mma.global.redis.utils.RedisUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Transactional(readOnly = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final FightEventRepository fightEventRepository;
    private final RedisUtils<StreamFightEventDto> redisUtils;
    private final S3Service s3Service;

    public FightEventDto getSchedule(LocalDate date, String name) {
        Optional<FightEvent> schedule;
        if (date != null)
            schedule = fightEventRepository.findByEventDate(date);
        else if (name != null)
            schedule = fightEventRepository.findByName(name);
        else
            throw new CustomException(CustomErrorCode.BAD_REQUEST_400);
        return schedule.map(
                fightEvent -> {
                    FightEventDto fightEventDto = FightEventDto.toDto(fightEvent);
                    fightEventDto.getFighterFightEvents().forEach(
                            fighterFightEventDto -> {
                                fighterFightEventDto.getWinner().setHeadshotUrl(s3Service.generateGetObjectPreSignedUrl(
                                        "headshot/" + fighterFightEventDto.getWinner().getName().replace(' ', '-') + ".png")
                                );
                                fighterFightEventDto.getLoser().setHeadshotUrl(s3Service.generateGetObjectPreSignedUrl(
                                        "headshot/" + fighterFightEventDto.getLoser().getName().replace(' ', '-') + ".png")
                                );
                            }
                    );
                    return fightEventDto;
                }
        ).orElse(null);
    }

    public StreamFightEventDto stream() {
        StreamFightEventDto streamFightEvent = redisUtils.getData("current-event");
        streamFightEvent.getFighterFightEvents()
                .forEach(
                        ffe -> {
                            ffe.getWinner().setHeadshotUrl(s3Service.generateGetObjectPreSignedUrl(
                                    "headshot/" + ffe.getWinner().getName().replace(' ', '-') + ".png")
                            );
                            ffe.getLoser().setHeadshotUrl(s3Service.generateGetObjectPreSignedUrl(
                                    "headshot/" + ffe.getLoser().getName().replace(' ', '-') + ".png")
                            );
                            ffe.getWinner().setBodyUrl(s3Service.generateGetObjectPreSignedUrl(
                                    "body/" + ffe.getWinner().getName().replace(' ', '-') + ".png")
                            );
                            ffe.getLoser().setBodyUrl(s3Service.generateGetObjectPreSignedUrl(
                                    "body/" + ffe.getLoser().getName().replace(' ', '-') + ".png")
                            );
                        }
                );
        return streamFightEvent;
    }

}
