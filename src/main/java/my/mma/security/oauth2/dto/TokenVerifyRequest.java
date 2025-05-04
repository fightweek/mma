package my.mma.security.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import my.mma.user.entity.User;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TokenVerifyRequest {

    private String domain;
    private String accessToken;
    private String email;
    @JsonProperty("socialId")
    private String providedSocialId;
    private String nickname;

    public User toEntity(){
        return User.builder()
                .role("ROLE_USER")
                .email(email)
                .username(domain+"_"+providedSocialId)
                .nickname(nickname)
                .build();
    }

}
