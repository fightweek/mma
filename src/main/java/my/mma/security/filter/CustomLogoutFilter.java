package my.mma.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomException;
import my.mma.security.JWTUtil;
import my.mma.security.repository.RefreshRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    //프론트엔드측 : 로컬 스토리지에 존재하는 Access 토큰 삭제 및 서버측 로그아웃 경로로 Refresh 토큰 전송
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    @Transactional
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        if(!requestURI.matches("^\\/auth/logout$")){
            filterChain.doFilter(request,response);
            return;
        }
        if(!request.getMethod().equals("POST")){
            filterChain.doFilter(request,response);
            return;
        }

        String refresh = request.getHeader("Refresh");
        if(refresh == null){
            handleException(response,"refreshToken is null",HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.extractCategory(refresh);
        if (!category.equals("refresh")) {
            handleException(response,"refresh category is not matched",HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        boolean isExist = refreshRepository.existsById(refresh);
        if (!isExist) {
            handleException(response,"refresh is not existing in DB",HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        //로그아웃 진행
        //Refresh 토큰 DB에서 제거
        refreshRepository.deleteById(refresh);
        //Refresh 토큰 Cookie 값 0
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("deleted refresh token successfully");
    }

    private void handleException(HttpServletResponse response, String message, int statusCode) throws IOException {
        log.warn(message);
        response.setStatus(statusCode);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }

}
