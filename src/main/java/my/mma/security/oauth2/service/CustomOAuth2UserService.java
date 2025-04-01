package my.mma.security.oauth2.service;

import lombok.extern.slf4j.Slf4j;
import my.mma.security.entity.Member;
import my.mma.security.repository.UserRepository;
import my.mma.security.oauth2.CustomOAuth2User;
import my.mma.security.oauth2.dto.GoogleResponse;
import my.mma.security.oauth2.dto.NaverResponse;
import my.mma.security.oauth2.dto.OAuth2Response;
import my.mma.security.oauth2.dto.UserDto;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
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
        // userRequest : 리소스 서버에서 제공되는 유저 정보
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("oAuth2User: {}",oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {
            return null;
        }
        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        Optional<Member> existData = userRepository.findByUsername(username);
        UserDto userDto = new UserDto();

        if (existData.isEmpty()) {
            Member userEntity = Member.builder()
                    .socialId(oAuth2Response.getProviderId())
                    .role("ROLE_USER")
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .username(username)
                    .build();
            userRepository.save(userEntity);
            userDto.setUsername(username);
            userDto.setName(oAuth2Response.getName());
            userDto.setRole("ROLE_USER");
        }
        else {
            existData.get().setEmail(oAuth2Response.getEmail());
            existData.get().setName(oAuth2Response.getName());
            userDto.setUsername(existData.get().getUsername());
            userDto.setName(oAuth2Response.getName());
            userDto.setRole(existData.get().getRole());
        }
        return new CustomOAuth2User(userDto);
    }
}