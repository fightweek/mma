package my.mma.security.oauth2.service;

import lombok.extern.slf4j.Slf4j;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import my.mma.security.oauth2.CustomOAuth2User;
import my.mma.security.oauth2.dto.GoogleResponse;
import my.mma.security.oauth2.dto.NaverResponse;
import my.mma.security.oauth2.dto.OAuth2Response;
import my.mma.security.oauth2.dto.TempUserDto;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//@Service
@Slf4j
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("custom oauth2 user service loadUser executed");
        // userRequest : 리소스 서버에서 제공되는 유저 정보
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("oAuth2User: {}", oAuth2User);

        OAuth2Response oAuth2Response = getOauth2Response(userRequest, oAuth2User);
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Optional<User> existData = userRepository.findByUsername(username);
        TempUserDto userDto = TempUserDto.builder()
                .password("hello")
                .role("ROLE_USER")
                .email(oAuth2Response.getEmail())
                .nickname(oAuth2Response.getName())
                .build();

        // 회원가입
        if (existData.isEmpty()) {
            User userEntity = User.builder()
                    .role("ROLE_USER")
                    .email(oAuth2Response.getEmail())
                    .nickname(oAuth2Response.getName())
                    .username(username)
                    .build();
            userRepository.save(userEntity);
        } else {
            existData.get().updateEmail(oAuth2Response.getEmail());
            existData.get().updateNickname(oAuth2Response.getName());
        }
        return new CustomOAuth2User(userDto);
    }

    private static OAuth2Response getOauth2Response(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }
        return oAuth2Response;
    }
}