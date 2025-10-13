package my.mma.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.FightEventDto;
import my.mma.event.entity.FightEvent;
import my.mma.event.repository.FightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.entity.TargetType;
import my.mma.global.repository.AlertRepository;
import my.mma.global.s3.service.S3ImgService;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final S3ImgService s3Service;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    public FightEventDto getSchedule(LocalDate date, String email) {
        Optional<FightEvent> schedule = fightEventRepository.findByEventDate(date);
        return schedule.map(
                fightEvent -> {
                    FightEventDto fightEventDto = FightEventDto.toDto(fightEvent);
                    if (fightEventDto.isUpcoming()) {
                        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(CustomErrorCode.SERVER_ERROR));
                        fightEventDto.setAlert(alertRepository.existsByUserAndTargetTypeAndTargetId(user, TargetType.EVENT, fightEvent.getId()));
                    }
                    fightEventDto.getFighterFightEvents().forEach(
                            fighterFightEventDto -> {
                                fighterFightEventDto.getWinner().setHeadshotUrl(s3Service.generateImgUrl(
                                        "headshot/" + fighterFightEventDto.getWinner().getName().replace(' ', '-') + ".png", 2)
                                );
                                fighterFightEventDto.getLoser().setHeadshotUrl(s3Service.generateImgUrl(
                                        "headshot/" + fighterFightEventDto.getLoser().getName().replace(' ', '-') + ".png", 2)
                                );
                            }
                    );
                    return fightEventDto;
                }
        ).orElse(null);
    }

    public Page<FightEventDto.FighterFightEventDto> search(String name, Pageable pageable) {
        Optional<Page<FightEvent>> events = fightEventRepository.findByNameContainingIgnoreCase(name, pageable);
        return events.map(
                page -> page.map(
                        fightEvent -> {
                            String winnerHeadshotUrl = s3Service.generateImgUrl(
                                    "headshot/" + fightEvent.getFighterFightEvents()
                                            .get(0).getWinner().getName().replace(' ', '-') + ".png", 2);
                            String loserHeadshotUrl = s3Service.generateImgUrl(
                                    "headshot/" + fightEvent.getFighterFightEvents()
                                            .get(0).getLoser().getName().replace(' ', '-') + ".png", 2);
                            FightEventDto.FighterFightEventDto mainCardDto = FightEventDto.FighterFightEventDto.toDto(fightEvent.getFighterFightEvents().get(0));
                            mainCardDto.getWinner().setHeadshotUrl(winnerHeadshotUrl);
                            mainCardDto.getLoser().setHeadshotUrl(loserHeadshotUrl);
                            return mainCardDto;
                        }
                )
        ).orElse(null);
    }
}
