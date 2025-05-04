package my.mma.security.config;

import lombok.RequiredArgsConstructor;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.security.filter.CustomLogoutFilter;
import my.mma.security.repository.RefreshRepository;
import my.mma.security.JWTUtil;
import my.mma.security.filter.JWTFilter;
import my.mma.security.filter.LoginFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.jwt.access.expiration}")
    private Long accessExpireMs;

    @Value("${spring.jwt.refresh.expiration}")
    private Long refreshExpireMs;


    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        try {
            return authenticationConfiguration.getAuthenticationManager();
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.SERVER_ERROR);
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
            corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
            corsConfiguration.setAllowCredentials(true);
            corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
            corsConfiguration.setMaxAge(3600L);
            corsConfiguration.setExposedHeaders(Collections.singletonList("Authorization"));
            return corsConfiguration;
        }));

//        http.oauth2Login((oauth2) -> oauth2.userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
//                        .userService(customOAuth2UserService))
//                .successHandler(customSuccessHandler));

        http.httpBasic(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(logout -> logout.logoutUrl("/auth/logout"));
//                .logoutSuccessHandler(
//                        (request, response, authentication) -> {
//                            response.setStatus(HttpServletResponse.SC_OK);
//                        }
//                ));

        http.authorizeHttpRequests(registry ->
                registry.requestMatchers("/", "/login", "/join", "/reissue",
                                "/mail/verify_code", "/mail/send_join_code", "/auth/social_login"
                        ).permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class); //LoginFilter 전에 필터 생성
        http.addFilterAt(new LoginFilter(authenticationManager(), jwtUtil, refreshRepository, accessExpireMs, refreshExpireMs), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);
        return http.build();
    }
}