package my.mma.security.controller;

import lombok.RequiredArgsConstructor;
import my.mma.exception.CustomException;
import my.mma.security.oauth2.dto.TokenResponse;
import my.mma.security.oauth2.dto.TokenVerifyRequest;
import my.mma.security.oauth2.service.OAuth2Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static my.mma.exception.CustomErrorCode.SOCIAL_TOKEN_VERIFY_FAILED_400;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OAuth2Controller {

    private final RestTemplate restTemplate;
    private final OAuth2Service oAuth2Service;

    // application.yml 또는 application.properties에 넣어둔 값 주입
    @PostMapping("/social_login")
    public ResponseEntity<TokenResponse> socialLogin(
            @RequestBody TokenVerifyRequest request
    ) {
        String verifyUrl = switch (request.domain()) {
            case "KAKAO" -> "https://kapi.kakao.com/v1/user/access_token_info";
            case "NAVER" -> "https://openapi.naver.com/v1/nid/verify";
            case "GOOGLE" -> "https://www.googleapis.com/oauth2/v1/userinfo";
            default -> null;
        };

        HttpHeaders headers = new HttpHeaders();
        System.out.println("request = " + request.accessToken());
        headers.add("Authorization", "Bearer " + request.accessToken());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                verifyUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("login success");
            return ResponseEntity.ok().body(oAuth2Service.saveUserIfNotExists(request));
        }
        throw new CustomException(SOCIAL_TOKEN_VERIFY_FAILED_400);
    }
}