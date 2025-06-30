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
import my.mma.global.dto.UpdatePreferenceDto;
import my.mma.global.entity.Alert;
import my.mma.global.entity.Like;
import my.mma.global.entity.TargetType;
import my.mma.global.repository.AlertRepository;
import my.mma.global.repository.LikeRepository;
import my.mma.global.s3.service.S3Service;
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
    private final LikeRepository likeRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public FighterDetailDto detail(String email, Long fighterId) {
        Fighter fighter = fighterRepository.findById(fighterId).orElseThrow(
                () -> new CustomException(CustomErrorCode.SERVER_ERROR)
        );
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
        boolean isLikeExists = likeRepository.existsByUserAndTargetTypeAndTargetId(user, TargetType.FIGHTER, fighterId);
        boolean isAlertExists = alertRepository.existsByUserAndTargetTypeAndTargetId(user, TargetType.FIGHTER, fighterId);
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
        return FighterDetailDto.toDto(fighter, fighterFightEventDtos, imgPreingedUrl, isLikeExists, isAlertExists);
    }

    @Transactional
    public void updatePreference(String email, UpdatePreferenceDto request) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
        if (request.getCategory().equals("LIKE")) {
            if (request.isOn()) {
                Like like = Like.builder()
                        .user(user)
                        .targetType(TargetType.FIGHTER)
                        .targetId(request.getTargetId())
                        .build();
                likeRepository.save(like);
            } else {
                likeRepository.deleteByUserAndTargetTypeAndTargetId(user, TargetType.FIGHTER, request.getTargetId());
            }
            return;
        } else if (request.getCategory().equals("ALERT")) {
            if (request.isOn()) {
                Alert alert = Alert.builder()
                        .user(user)
                        .targetType(TargetType.FIGHTER)
                        .targetId(request.getTargetId())
                        .build();
                alertRepository.save(alert);
            } else {
                alertRepository.deleteByUserAndTargetTypeAndTargetId(user, TargetType.FIGHTER, request.getTargetId());
            }
            return;
        }
        throw new CustomException(CustomErrorCode.BAD_REQUEST_400);
    }

    public Page<FighterDto> search(String name, Pageable pageable) {
        Optional<Page<Fighter>> fighters = fighterRepository.findByNameContainingIgnoreCase(name, pageable);
        return fighters.map(
                page -> page.map(
                        f -> {
                            FighterDto fighter = FighterDto.toDto(f);
                            fighter.setImgPresignedUrl(s3Service.generateGetObjectPreSignedUrl(
                                    "headshot/" + fighter.getName().replace(' ', '-') + ".png"));
                            return fighter;
                        }
                )
        ).orElse(null);
    }
}
