package my.mma.fighter.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.FightEventDto.FighterFightEventDto;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.repository.FighterFightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.dto.FighterDetailDto;
import my.mma.fighter.dto.FighterDto;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.global.entity.TargetType;
import my.mma.global.repository.AlertRepository;
import my.mma.global.s3.service.S3ImgService;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                () -> new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR)
        );
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
        boolean isAlertExists = alertRepository.existsByUserAndTargetTypeAndTargetId(user, TargetType.FIGHTER, fighterId);
        Optional<List<FighterFightEvent>> fighterFightEvents = fighterFightEventRepository.findByFighter(fighter);
        List<FighterFightEventDto> fighterFightEventDtos = fighterFightEvents.map(
                ffeList -> ffeList.stream().map(FighterFightEventDto::toDto)
                        .collect(Collectors.toList())).orElse(null);
        if (fighterFightEventDtos != null)
            fighterFightEventDtos.forEach(
                    ffe -> {
                        ffe.getWinner().setHeadshotUrl(s3Service.generateImgUrl(
                                "headshot/" + ffe.getWinner().getName().replace(' ', '-') + ".png", 2));
                        ffe.getLoser().setHeadshotUrl(s3Service.generateImgUrl(
                                "headshot/" + ffe.getLoser().getName().replace(' ', '-') + ".png", 2));
                    }
            );
        String bodyUrl = s3Service.generateImgUrl(
                "body/" + fighter.getName().replace(' ', '-') + ".png", 2);
        return FighterDetailDto.toDto(fighter, fighterFightEventDtos, bodyUrl, isAlertExists);
    }

    public Page<FighterDto> search(String name, Pageable pageable) {
        Optional<Page<Fighter>> fighters = fighterRepository.findByNameContainingIgnoreCase(name, pageable);
        return fighters.map(
                page -> page.map(
                        fighter -> {
                            FighterDto fighterDto = FighterDto.toDto(fighter);
                            fighterDto.setHeadshotUrl(s3Service.generateImgUrl(
                                    "headshot/" + fighterDto.getName().replace(' ', '-') + ".png", 2));
                            return fighterDto;
                        }
                )
        ).orElse(null);
    }
}
