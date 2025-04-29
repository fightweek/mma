package my.mma.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.security.JWTUtil;
import my.mma.security.entity.Refresh;
import my.mma.security.repository.RefreshRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;

// refresh 토큰의 사용처는 /reissue 하나이므로, CSRF 공격에 취약해져도 무방 (따라서 쿠키에 저장해도 됨)
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reissue")
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    // refresh 토큰을 통해 access 토큰이 만료되었을 때 새로운 access 및 refresh 토큰 재발급
    @PostMapping("")
    public ResponseEntity<?> reissue_refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refresh = request.getHeader("Refresh");
        if(refresh == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("refresh token is null");
        }
        jwtUtil.validateToken(refresh);

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.extractCategory(refresh);
        if (!category.equals("refresh")) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }
        boolean isExist = refreshRepository.existsById(refresh);
        if (!isExist) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }


        String email = jwtUtil.extractEmail(refresh);
        String role = jwtUtil.extractRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", email, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", email, role, 86400000L);

        // refresh rotate (기존 refresh 토큰 삭제, 새로운 refresh 토큰 생성 및 DB에 저장 => 로그인 지속 시간 증가)
        refreshRepository.deleteById(refresh);
        addRefreshEntity(email, newRefresh, 86400000L);

        //response
        HashMap<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccess);
        tokens.put("refreshToken", refresh);
        return ResponseEntity.ok().body(tokens);
    }

    private void addRefreshEntity(String email, String refresh, Long expiredMs) {
        refreshRepository.save(Refresh.builder()
                .email(email)
                .token(refresh)
                .expiration(expiredMs)
                .build());
    }

}