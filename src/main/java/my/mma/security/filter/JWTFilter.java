package my.mma.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomException;
import my.mma.security.CustomUserDetails;
import my.mma.security.JWTUtil;
import my.mma.security.oauth2.dto.TempUserDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JWT Filter doFilterInternal execute");
        String refresh = request.getHeader("Refresh");
        if(refresh != null){
            filterChain.doFilter(request,response);
            return;
        }
        String authorization = request.getHeader("Authorization");

        // 토큰이 없다면 다음 필터로 넘김
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.info("accessToken is null");
            filterChain.doFilter(request, response);
            return;
        }
        String accessToken = authorization.split(" ")[1];
        // 더욱 세밀하게 (만료/페이로드 문제 등) 검사도 가능하지만, 일단 유효성 검증 실패 응답만
        try {
            jwtUtil.validateToken(accessToken);
        }catch (CustomException e){
            handleException(response,"Invalid jwt token",HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!jwtUtil.extractCategory(accessToken).equals("access")) {
            handleException(response, "Invalid token category", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        TempUserDto userDto = TempUserDto.builder()
                .role(jwtUtil.extractRole(accessToken))
                .email(jwtUtil.extractEmail(accessToken))
                .password("social_login_no_password")
                .build();
        CustomUserDetails customUserDetails = new CustomUserDetails(userDto);
        // 일시적으로 생성되는 세션 (사용자 등록)
        System.out.println("user = " + userDto);
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private void handleException(HttpServletResponse response, String message, int statusCode) throws IOException {
        log.warn(message);
        response.setStatus(statusCode);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}