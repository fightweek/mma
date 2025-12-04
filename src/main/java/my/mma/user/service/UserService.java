package my.mma.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.alert.constant.AlertTarget;
import my.mma.alert.entity.UserPreferences;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.user.dto.JoinRequest;
import my.mma.user.dto.UserDto;
import my.mma.user.dto.WithdrawalReasonDto;
import my.mma.user.entity.User;
import my.mma.user.entity.WithdrawalReason;
import my.mma.user.entity.WithdrawnUserEmail;
import my.mma.alert.repository.UserPreferencesRepository;
import my.mma.user.repository.UserRepository;
import my.mma.user.repository.WithdrawalReasonRepository;
import my.mma.user.repository.WithdrawnEmailRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WithdrawalReasonRepository withdrawalReasonRepository;
    private final WithdrawnEmailRepository withdrawnEmailRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean checkDuplicatedNickname(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    @Transactional
    public UserDto updateNickname(String email, String nickname) {
        User user = getUser(email);
        user.updateNickname(nickname);
        return UserDto.toDto(user);
    }

    @Transactional
    public void updatePassword(String email, String password) {
        User user = getUser(email);
        user.updatePassword(bCryptPasswordEncoder.encode(password));
    }

    public UserDto getMe(String email) {
        return UserDto.toDto(getUser(email));
    }

    @Transactional
    public void join(JoinRequest request) {
        if (withdrawnEmailRepository.findById(request.email()).isPresent())
            throw new CustomException(CustomErrorCode.WITHDRAWN_USER_403);
        userRepository.findByEmail(request.email()).ifPresent(
                user -> {
                    throw new CustomException(CustomErrorCode.DUPLICATED_EMAIL_400);
                }
        );
        userRepository.findByNickname(request.nickname()).ifPresent(
                user -> {
                    throw new CustomException(CustomErrorCode.DUPLICATED_NICKNAME_400);
                }
        );
        User user = userRepository.save(
                User.builder()
                        .point(0)
                        .role("ROLE_USER")
                        .email(request.email())
                        .password(bCryptPasswordEncoder.encode(request.password()))
                        .nickname(request.nickname())
                        .build()
        );
        userPreferencesRepository.save(UserPreferences.builder()
                .user(user)
                .build());
    }

    @Transactional
    public void delete(String email, WithdrawalReasonDto withdrawalDto) {
        User user = getUser(email);
        userRepository.deleteById(user.getId());
        System.out.println("email = " + email);
        withdrawnEmailRepository.save(WithdrawnUserEmail.builder()
                .email(user.getEmail())
                .expiration(Duration.ofDays(7).toSeconds())
                .build());
        withdrawalReasonRepository.save(
                WithdrawalReason.builder()
                        .userId(user.getId())
                        .withdrawalCategory(withdrawalDto.category())
                        .description(withdrawalDto.description())
                        .build()
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400));
    }

}
