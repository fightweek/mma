package my.mma.security.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.security.CustomSuccessHandler;
import my.mma.security.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reissue")
public class ReissueController{

    private final JWTUtil jwtUtil;
    private final CustomSuccessHandler customSuccessHandler;

    @PostMapping("")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response){
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
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        //response
        response.addCookie(customSuccessHandler.createCookie("access", newAccess));
        return new ResponseEntity<>(HttpStatus.OK);
    }

}