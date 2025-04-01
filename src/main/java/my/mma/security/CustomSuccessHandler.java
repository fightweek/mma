//package my.mma.security;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import my.mma.security.entity.Refresh;
//import my.mma.security.oauth2.CustomOAuth2User;
//import my.mma.security.repository.RefreshRepository;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Date;
//import java.util.Iterator;
//
//@Component
//@Slf4j
//public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final JWTUtil jwtUtil;
//    private final RefreshRepository refreshRepository;
//
//    public CustomSuccessHandler(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
//        this.jwtUtil = jwtUtil;
//        this.refreshRepository = refreshRepository;
//    }
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        //OAuth2User
//        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
//        String username = customUserDetails.getUsername();
//        log.info("username={}",username);
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
//        GrantedAuthority auth = iterator.next();
//        String role = auth.getAuthority();
//
//        String refreshToken = jwtUtil.createJwt("refresh",username,role,86400000L);
//        addRefreshEntity(username,refreshToken,86400000L);
//        response.addCookie(createCookie("refresh", refreshToken));
//        response.sendRedirect("http://localhost:8080");
//    }
//
//    private void addRefreshEntity(String username,String refresh,Long expiredMs){
//        Date date = new Date(System.currentTimeMillis()*expiredMs);
//        refreshRepository.save(Refresh.builder()
//                .username(username)
//                .refresh(refresh)
//                .expiration(date.toString())
//                .build());
//    }
//
//    public Cookie createCookie(String key, String value) {
//        Cookie cookie = new Cookie(key, value);
//        cookie.setMaxAge(24*60*60);
//        //cookie.setSecure(true);
//        cookie.setPath("/");
//        cookie.setHttpOnly(true);
//
//        return cookie;
//    }
//}