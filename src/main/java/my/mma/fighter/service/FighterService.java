package my.mma.fighter.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fightevent.dto.FightEventDto.FighterFightEventDto;
import my.mma.fightevent.entity.FighterFightEvent;
import my.mma.fightevent.repository.FighterFightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.dto.FighterDetailDto;
import my.mma.fighter.dto.FighterDto;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.alert.constant.AlertTarget;
import my.mma.alert.repository.AlertRepository;
import my.mma.global.s3.service.S3ImgService;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static my.mma.global.s3.service.S3ImgService.BODY_OBJECT_KEY_PREFIX;
import static my.mma.global.s3.service.S3ImgService.HEADSHOT_OBJECT_KEY_PREFIX;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FighterService {

    private final FighterRepository fighterRepository;
    private final FighterFightEventRepository fighterFightEventRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final S3ImgService s3Service;

    public FighterDetailDto detail(String email, Long fighterId) {
        Fighter fighter = fighterRepository.findById(fighterId).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_FIGHTER_CONFIGURED_400)
        );
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
        boolean isAlertExists = alertRepository.existsByUserAndAlertTargetAndTargetId(user, AlertTarget.FIGHTER, fighterId);
        List<FighterFightEvent> fighterFightEvents = fighterFightEventRepository.findByFighter(fighter);
        List<FighterFightEventDto> fighterFightEventDtos = fighterFightEvents.stream()
                .map(FighterFightEventDto::toDto)
                .peek(ffe -> {
                    ffe.getWinner().setHeadshotUrl(getFighterImgUrl(ffe.getWinner().getName(), HEADSHOT_OBJECT_KEY_PREFIX));
                    ffe.getLoser().setHeadshotUrl(getFighterImgUrl(ffe.getLoser().getName(), HEADSHOT_OBJECT_KEY_PREFIX));
                }).toList();
        String bodyUrl = getFighterImgUrl(fighter.getName(), BODY_OBJECT_KEY_PREFIX);
        return FighterDetailDto.toDto(fighter, fighterFightEventDtos, bodyUrl, isAlertExists);
    }

    public Page<FighterDto> search(String name, Pageable pageable) {
        Optional<Page<Fighter>> fighters = fighterRepository.findByNameContainingIgnoreCase(name, pageable);
        return fighters.map(
                page -> page.map(
                        fighter -> {
                            FighterDto fighterDto = FighterDto.toDto(fighter);
                            fighterDto.setHeadshotUrl(getFighterImgUrl(fighterDto.getName(), HEADSHOT_OBJECT_KEY_PREFIX));
                            return fighterDto;
                        }
                )
        ).orElse(null);
    }

    private String getFighterImgUrl(String name, String objectKey) {
        return s3Service.generateImgUrl(
                objectKey + name.replace(' ', '-') + ".png", 2);
    }
}
