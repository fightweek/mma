package my.mma.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.user.dto.JoinRequest;
import my.mma.user.dto.UserDto;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean checkDuplicatedNickname(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    @Transactional
    public UserDto updateNickname(String email, String nickname) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400));
        user.updateNickname(nickname);
        return UserDto.toDto(user);
    }

    public UserDto getMe(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400));
        return UserDto.toDto(user);
    }

    @Transactional
    public void join(JoinRequest request) {
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

}
