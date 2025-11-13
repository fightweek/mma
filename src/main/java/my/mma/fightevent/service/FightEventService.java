package my.mma.fightevent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fightevent.dto.FightEventDto;
import my.mma.fightevent.dto.FightEventDto.FighterFightEventDto;
import my.mma.fightevent.dto.FighterFightEventCardDetailDto;
import my.mma.fightevent.dto.StreamFightEventDto.FighterFightEventCardFighterDto;
import my.mma.fightevent.entity.FightEvent;
import my.mma.fightevent.entity.FighterFightEvent;
import my.mma.fightevent.repository.FightEventRepository;
import my.mma.fightevent.repository.FighterFightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.entity.Fighter;
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

import static my.mma.exception.CustomErrorCode.*;

@Transactional(readOnly = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class FightEventService {

    private final FightEventRepository fightEventRepository;
    private final FighterFightEventRepository fighterFightEventRepository;
    private final S3ImgService s3Service;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    public FightEventDto getSchedule(LocalDate date, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(NO_SUCH_USER_CONFIGURED_400));
        Optional<FightEvent> schedule = fightEventRepository.findByEventDate(date);
        return schedule.map(
                fightEvent -> {
                    FightEventDto fightEventDto = FightEventDto.toDto(fightEvent);
                    if (fightEventDto.isUpcoming()) {
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

    public Page<FighterFightEventDto> search(String name, Pageable pageable) {
        Optional<Page<FightEvent>> events = fightEventRepository.findByNameContainingIgnoreCase(name, pageable);
        return events.map(
                page -> page.map(
                        this::getMainCardDto
                )
        ).orElse(null);
    }

    public FighterFightEventDto getMainCardDto(FightEvent fightEvent) {
        String winnerHeadshotUrl = s3Service.generateImgUrl(
                "headshot/" + fightEvent.getFighterFightEvents()
                        .get(0).getWinner().getName().replace(' ', '-') + ".png", 2);
        String loserHeadshotUrl = s3Service.generateImgUrl(
                "headshot/" + fightEvent.getFighterFightEvents()
                        .get(0).getLoser().getName().replace(' ', '-') + ".png", 2);
        FighterFightEventDto mainCardDto = FighterFightEventDto.toDto(fightEvent.getFighterFightEvents().get(0));
        mainCardDto.getWinner().setHeadshotUrl(winnerHeadshotUrl);
        mainCardDto.getLoser().setHeadshotUrl(loserHeadshotUrl);
        return mainCardDto;
    }

    public FighterFightEventCardDetailDto cardDetail(Long ffeId){
        FighterFightEvent ffe = fighterFightEventRepository.findById(ffeId)
                .orElseThrow(() -> new CustomException(BAD_REQUEST_400));

        Fighter winner = ffe.getWinner();
        Fighter loser = ffe.getLoser();

        String winnerBodyUrl = s3Service.generateImgUrl(
                "body/" + winner.getName().replace(' ', '-') + ".png", 2);
        String loserBodyUrl = s3Service.generateImgUrl(
                "body/" +loser.getName().replace(' ', '-') + ".png", 2);
        FighterFightEventCardFighterDto winnerCardDto = FighterFightEventCardFighterDto.toDto(winner);
        FighterFightEventCardFighterDto loserCardDto = FighterFightEventCardFighterDto.toDto(loser);
        winnerCardDto.setBodyUrl(winnerBodyUrl);
        loserCardDto.setBodyUrl(loserBodyUrl);
        return new FighterFightEventCardDetailDto(winnerCardDto,loserCardDto,ffe.getFightWeight());
    }
}
