package my.mma.security.oauth2.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public record TokenResponse(String accessToken, String refreshToken) {

    public static TokenResponse toDto(String accessToken, String refreshToken){
        return new TokenResponse(accessToken, refreshToken);
    }

}
