package my.mma.fixture.user;

import my.mma.user.entity.User;

public class UserFixture {

    public static User createUser(){
        return userBuilderWithoutEmail()
                .email("defaultemail123@google.com")
                .build();
    }

    public static User createUserWithEmail(String email){
        return userBuilderWithoutEmail()
                .email(email)
                .build();
    }

    public static User.UserBuilder userBuilderWithoutEmail(){
        return User.builder()
                .id(1L)
                .nickname("nickname123")
                .role("ROLE_USER")
                .password("pwd123")
                .fcmToken("abcdef")
                .point(1000);
    }

}
