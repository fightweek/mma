package my.mma.security.oauth2.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;

    public static TokenResponse toDto(String accessToken, String refreshToken, boolean isCreated){
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
