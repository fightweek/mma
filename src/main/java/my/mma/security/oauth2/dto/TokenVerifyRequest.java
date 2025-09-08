package my.mma.security.oauth2.dto;

import my.mma.user.entity.User;

public record TokenVerifyRequest(String domain, String accessToken, String email, String socialId, String fcmToken) {

    public User toEntity(){
        return User.builder()
                .role("ROLE_USER")
                .email(email)
                .point(1000)
                .username(domain+"_"+socialId)
                .fcmToken(fcmToken)
                .build();
    }

}
