package my.mma.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.bet.repository.BetRepository;
import my.mma.event.dto.FightEventDto;
import my.mma.event.repository.FightEventRepository;
import my.mma.event.service.EventService;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.dto.FighterDto;
import my.mma.fighter.repository.FighterRepository;
import my.mma.global.entity.Alert;
import my.mma.global.entity.TargetType;
import my.mma.global.repository.AlertRepository;
import my.mma.global.s3.service.S3ImgService;
import my.mma.user.dto.UserBetRecord;
import my.mma.user.dto.UserProfileDto;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final BetRepository betRepository;
    private final AlertRepository alertRepository;
    private final FighterRepository fighterRepository;
    private final FightEventRepository fightEventRepository;
    private final S3ImgService s3Service;
    private final EventService eventService;

    public UserProfileDto profile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400));
        UserBetRecord userBetRecord = betRepository.getUserBetRecord(user.getId()); // nullable
        List<Alert> userAlerts = alertRepository.findByUserId(user.getId());

        List<Long> fighterIds = userAlerts.stream()
                .filter(alert -> alert.getTargetType().equals(TargetType.FIGHTER))
                .map(Alert::getTargetId)
                .toList();
        List<FighterDto> alertFighters = fighterRepository.findAllById(fighterIds)
                .stream().map(fighter -> {
                    FighterDto fighterDto = FighterDto.toDto(fighter);
                    fighterDto.setHeadshotUrl(s3Service.generateImgUrl(
                            "headshot/" + fighterDto.getName().replace(' ', '-') + ".png", 2));
                    return fighterDto;
                }).toList();

        List<Long> fightEventIds = userAlerts.stream()
                .filter(alert -> alert.getTargetType().equals(TargetType.EVENT))
                .map(Alert::getTargetId)
                .toList();
        List<FightEventDto.FighterFightEventDto> alertFightEvents = fightEventRepository.findAllById(fightEventIds)
                .stream().map(eventService::getMainCardDto).toList();

        return UserProfileDto.builder()
                .userBetRecord(userBetRecord)
                .alertFighters(alertFighters)
                .alertEvents(alertFightEvents)
                .build();
    }

}
