package my.mma.security.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import my.mma.user.entity.User;

import java.util.UUID;

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

    public User toEntity(){
        return User.builder()
                .role("ROLE_USER")
                .email(email)
                .point(1000)
//                .nickname("guest_"+UUID.randomUUID())
                .username(domain+"_"+providedSocialId)
                .build();
    }

}
