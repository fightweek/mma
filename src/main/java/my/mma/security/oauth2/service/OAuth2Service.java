package my.mma.security.oauth2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.security.JWTUtil;
import my.mma.security.dto.JwtCrateDto;
import my.mma.security.entity.Refresh;
import my.mma.security.oauth2.dto.TokenVerifyRequest;
import my.mma.security.oauth2.dto.TokenResponse;
import my.mma.security.repository.RefreshRepository;
import my.mma.security.repository.UserRepository;
import my.mma.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.Optional;

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
    private final JWTUtil jwtUtil;

    @Transactional
    public TokenResponse saveUserIfNotExists(TokenVerifyRequest request) {
        String access = jwtUtil.createJwt(JwtCrateDto.toDto(
                "access", request.getEmail(), "ROLE_USER", accessExpireMs, request.getDomain(), true
        ));
        String refresh = jwtUtil.createJwt(JwtCrateDto.toDto(
                "refresh", request.getEmail(), "ROLE_USER", refreshExpireMs, request.getDomain(), true
        ));
        addRefreshEntity(request.getEmail(), refresh, refreshExpireMs);
        // (소셜 로그인 시도) 중복 이메일 & (다른 소셜 플랫폼 or 일반 로그인 계정) -> 로그인 안 되도록 설정, 프론트는 알림 문구 띄움
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getUsername() == null || !user.getUsername().startsWith(request.getDomain()))
                throw new CustomException(CustomErrorCode.DUPLICATED_EMAIL_403);
        }
        if (userRepository.findByUsername(request.getDomain() + "_" + request.getProvidedSocialId()).isEmpty()) {
            userRepository.save(request.toEntity());
            return TokenResponse.toDto(access, refresh, true);
        }
        return TokenResponse.toDto(access, refresh, false);
    }

    private void addRefreshEntity(String email, String refresh, Long expiredMs) {
        refreshRepository.save(Refresh.builder()
                .email(email)
                .token(refresh)
                .expiration(expiredMs)
                .build());
    }

}
