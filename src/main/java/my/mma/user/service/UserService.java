package my.mma.user.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.security.JWTUtil;
import my.mma.security.repository.UserRepository;
import my.mma.user.dto.UserDto;
import my.mma.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    public UserDto getMe(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization").split(" ")[1];
        String email = jwtUtil.extractEmail(accessToken);
        User user;
        if (jwtUtil.extractIsSocial(accessToken)) {
            String domain = jwtUtil.extractDomain(accessToken);
            user = userRepository.findByEmailAndUsernameStartingWith(email, domain).orElseThrow(
                    () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_500)
            );
        } else {
            user = userRepository.findByEmailAndUsernameIsNull(email).orElseThrow(
                    () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_500)
            );
        }
        return UserDto.toDto(user);
    }

}
