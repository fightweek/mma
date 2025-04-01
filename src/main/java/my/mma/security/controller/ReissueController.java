package my.mma.security.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
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

import java.util.Date;

// refresh 토큰의 사용처는 /reissue 하나이므로, CSRF 공격에 취약해져도 무방 (따라서 쿠키에 저장해도 됨)
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reissue")
public class ReissueController{

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> reissue_refresh(HttpServletRequest request, HttpServletResponse response){
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("refresh")){
                refresh = cookie.getValue();
            }
        }
        if(refresh == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("refresh token is null");
        }
        try {
            jwtUtil.isExpired(refresh);
        }catch (ExpiredJwtException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("refresh token is expired");
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if(!isExist){
            return new ResponseEntity<>("invalid refresh token",HttpStatus.BAD_REQUEST);
        }


        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh",username,role,86400000L);

        // refresh rotate (기존 refresh 토큰 삭제, 새로운 refresh 토큰 생성 및 DB에 저장 => 로그인 지속 시간 증가)
        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(username,newRefresh,86400000L);

        //response
        response.setHeader("access",newAccess);
        response.setHeader("refresh",newRefresh);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void addRefreshEntity(String username,String refresh,Long expiredMs){
        Date date = new Date(System.currentTimeMillis()*expiredMs);
        refreshRepository.save(Refresh.builder()
                .username(username)
                .refresh(refresh)
                .expiration(date.toString())
                .build());
    }


}