package my.mma.security.oauth2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.alert.entity.UserPreferences;
import my.mma.alert.repository.UserPreferencesRepository;
import my.mma.exception.CustomException;
import my.mma.security.JWTUtil;
import my.mma.security.dto.JwtCrateDto;
import my.mma.security.entity.Refresh;
import my.mma.security.oauth2.dto.TokenResponse;
import my.mma.security.oauth2.dto.TokenVerifyRequest;
import my.mma.security.repository.RefreshRepository;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import my.mma.user.repository.WithdrawnEmailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static my.mma.exception.CustomErrorCode.DUPLICATED_EMAIL_403;
import static my.mma.exception.CustomErrorCode.WITHDRAWN_USER_403;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class OAuth2Service {

    @Value("${spring.jwt.refresh.expiration}")
    private Long refreshExpireMs;

    @Value("${spring.jwt.access.expiration}")
    private Long accessExpireMs;

    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;
    private final WithdrawnEmailRepository withdrawnEmailRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final JWTUtil jwtUtil;

    @Transactional
    public TokenResponse saveUserIfNotExists(TokenVerifyRequest request) {
        String access = jwtUtil.createJwt(JwtCrateDto.toDto(
                "access", request.email(), "ROLE_USER", accessExpireMs, request.domain(), true
        ));
        String refresh = jwtUtil.createJwt(JwtCrateDto.toDto(
                "refresh", request.email(), "ROLE_USER", refreshExpireMs, request.domain(), true
        ));
        if (withdrawnEmailRepository.findById(request.email()).isPresent())
            throw new CustomException(WITHDRAWN_USER_403);
        // (소셜 로그인 시도) 중복 이메일 & (다른 소셜 플랫폼 or 일반 로그인 계정) -> 로그인 안 되도록 설정, 프론트는 알림 문구 띄움
        Optional<User> userOpt = userRepository.findByEmail(request.email());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // username == null : 일반 회원가입 사용자 / !user.getUsername().startsWith(request.domain()) : 소셜 회원가입 사용자
            if (user.getUsername() == null || !user.getUsername().startsWith(request.domain()))
                throw new CustomException(DUPLICATED_EMAIL_403);
            else
                user.updateFcmToken(request.fcmToken());
        }
        addRefreshEntity(request.email(), refresh, refreshExpireMs);
        if (userRepository.findByUsername(request.domain() + "_" + request.socialId()).isEmpty()) {
            User user = userRepository.save(request.toEntity());
            userPreferencesRepository.save(UserPreferences.builder()
                    .user(user)
                    .build());
            return TokenResponse.toDto(access, refresh);
        }
        return TokenResponse.toDto(access, refresh);
    }

    private void addRefreshEntity(String email, String refresh, Long expiredMs) {
        refreshRepository.save(Refresh.builder()
                .email(email)
                .token(refresh)
                .expiration(expiredMs)
                .build());
    }

}
