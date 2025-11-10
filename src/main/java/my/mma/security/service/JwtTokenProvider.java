package my.mma.security.service;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomException;
import my.mma.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static my.mma.exception.CustomErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private SecretKey secretKey;

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.access.expiration}")
    private String accessTokenValidTime;

    @Value("${spring.jwt.refresh.expiration}")
    private String refreshTokenValidTime;

    @PostConstruct
    private void init(){
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String createJwt(String category, String email, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category",category)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String extractToken(HttpServletRequest request){
        return request.getHeader("Authorization").split(" ")[1];
    }

    public String extractEmail(String access){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(access).getPayload().get("email", String.class);
    }

    public Boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(JWT_TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(UNSUPPORTED_JWT);
        } catch (Exception e) {
            // 여기에서 예외 메시지를 설정해야 합니다.
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }

}
