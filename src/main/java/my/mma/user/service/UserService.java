package my.mma.user.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.security.JWTUtil;
import my.mma.user.repository.UserRepository;
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

    public boolean checkDuplicatedNickname(String nickname){
        System.out.println(nickname);
        return userRepository.findByNickname(nickname).isPresent();
    }

    @Transactional
    public UserDto updateNickname(HttpServletRequest request, String nickname){
        System.out.println("nickname = " + nickname);
        String accessToken = request.getHeader("Authorization").split(" ")[1];
        String email = jwtUtil.extractEmail(accessToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(CustomErrorCode.SERVER_ERROR));
        user.updateNickname(nickname);
        return UserDto.toDto(user);
    }

    @Transactional
    public void updateFcmToken(String email, String fcmToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SERVER_ERROR));
        user.updateFcmToken(fcmToken);
    }

    public UserDto getMe(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization").split(" ")[1];
        String email = jwtUtil.extractEmail(accessToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400));
        return UserDto.toDto(user);
    }

}
