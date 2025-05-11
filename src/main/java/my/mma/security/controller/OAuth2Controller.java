package my.mma.security.controller;

import lombok.RequiredArgsConstructor;
import my.mma.exception.CustomException;
import my.mma.security.oauth2.dto.TokenVerifyRequest;
import my.mma.security.oauth2.dto.TokenResponse;
import my.mma.security.oauth2.service.OAuth2Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static my.mma.exception.CustomErrorCode.SOCIAL_TOKEN_VERIFY_FAILED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OAuth2Controller {

    private final RestTemplate restTemplate;
    private final OAuth2Service oAuth2Service;

    // application.yml 또는 application.properties에 넣어둔 값 주입
    @PostMapping("/social_login")
    public ResponseEntity<TokenResponse> requestNaverToken(
            @RequestBody TokenVerifyRequest request
    ) {
        String verifyUrl = null;
        if(request.getDomain().equals("KAKAO")){
            verifyUrl = "https://kapi.kakao.com/v1/user/access_token_info";
        }
        else if(request.getDomain().equals("NAVER")){
            verifyUrl = "https://openapi.naver.com/v1/nid/verify";
        }
        else if(request.getDomain().equals("GOOGLE")){
            verifyUrl = "https://www.googleapis.com/oauth2/v1/userinfo";
        }

        // form 파라미터 준비
        // Header - Content-Type
        HttpHeaders headers = new HttpHeaders();
        System.out.println("request = " + request.getAccessToken());
        headers.add("Authorization", "Bearer " + request.getAccessToken());
        // form 데이터 entity
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // POST 요청 (RestTemplate)
        assert verifyUrl != null;
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
        throw new CustomException(SOCIAL_TOKEN_VERIFY_FAILED);
    }
}