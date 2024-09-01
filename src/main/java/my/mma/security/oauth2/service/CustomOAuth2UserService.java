package my.mma.security.oauth2.service;

import my.mma.security.entity.UserEntity;
import my.mma.security.repository.UserRepository;
import my.mma.security.oauth2.CustomOAuth2User;
import my.mma.security.oauth2.dto.GoogleResponse;
import my.mma.security.oauth2.dto.NaverResponse;
import my.mma.security.oauth2.dto.OAuth2Response;
import my.mma.security.dto.UserDto;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

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
        Optional<UserEntity> existData = userRepository.findByUsername(username);

        if (existData.isEmpty()) {
            UserEntity userEntity = UserEntity.builder()
                    .socialId(oAuth2Response.getProviderId())
                    .role("ROLE_USER")
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .build();
            userRepository.save(userEntity);
            UserDto userDto = new UserDto();
            userDto.setUsername(username);
            userDto.setName(oAuth2Response.getName());
            userDto.setRole("ROLE_USER");
            return new CustomOAuth2User(userDto);
        }
        else {
            existData.get().setEmail(oAuth2Response.getEmail());
            existData.get().setName(oAuth2Response.getName());
            UserDto userDto = new UserDto();
            userDto.setUsername(existData.get().getUsername());
            userDto.setName(oAuth2Response.getName());
            userDto.setRole(existData.get().getRole());
            return new CustomOAuth2User(userDto);
        }
    }
}