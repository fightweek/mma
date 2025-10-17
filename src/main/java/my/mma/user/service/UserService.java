package my.mma.user.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.bet.repository.BetRepository;
import my.mma.event.dto.FightEventDto;
import my.mma.event.dto.FightEventDto.FighterFightEventDto;
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
import my.mma.security.JWTUtil;
import my.mma.user.dto.JoinRequest;
import my.mma.user.dto.UserBetRecord;
import my.mma.user.dto.UserProfileDto;
import my.mma.user.repository.UserRepository;
import my.mma.user.dto.UserDto;
import my.mma.user.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BetRepository betRepository;
    private final AlertRepository alertRepository;
    private final FighterRepository fighterRepository;
    private final FightEventRepository fightEventRepository;
    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final S3ImgService s3Service;
    private final EventService eventService;

    public boolean checkDuplicatedNickname(String nickname) {
        System.out.println(nickname);
        return userRepository.findByNickname(nickname).isPresent();
    }

    @Transactional
    public UserDto updateNickname(HttpServletRequest request, String nickname) {
        System.out.println("nickname = " + nickname);
        String accessToken = request.getHeader("Authorization").split(" ")[1];
        String email = jwtUtil.extractEmail(accessToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(CustomErrorCode.SERVER_ERROR));
        user.updateNickname(nickname);
        return UserDto.toDto(user);
    }

    public UserDto getMe(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization").split(" ")[1];
        String email = jwtUtil.extractEmail(accessToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400));
        return UserDto.toDto(user);
    }

    @Transactional
    public void join(JoinRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(
                user -> {
                    throw new CustomException(CustomErrorCode.BAD_REQUEST_400, "중복된 이메일");
                }
        );
        userRepository.findByNickname(request.nickname()).ifPresent(
                user -> {
                    throw new CustomException(CustomErrorCode.BAD_REQUEST_400, "중복된 닉네임");
                }
        );
        userRepository.save(
                User.builder()
                        .point(0)
                        .role("ROLE_USER")
                        .email(request.email())
                        .password(bCryptPasswordEncoder.encode(request.password()))
                        .nickname(request.nickname())
                        .build()
        );
    }

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
        List<FighterFightEventDto> alertFightEvents = fightEventRepository.findAllById(fightEventIds)
                .stream().map(eventService::getMainCardDto).toList();

        return UserProfileDto.builder()
                .userBetRecord(userBetRecord)
                .alertFighters(alertFighters)
                .alertEvents(alertFightEvents)
                .build();
    }
}
