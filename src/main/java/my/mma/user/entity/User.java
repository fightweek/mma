package my.mma.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "users")
@Builder
@AllArgsConstructor
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    // 'google'/'naver'/'kakao' + 해당 도메인으로부터 발급하는 아이디(번호)
    private String username;

    private String nickname;

    private String password;

    private String role;

    public void updateEmail(String email){
        this.email = email;
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

}