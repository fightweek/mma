package my.mma.user.entity;

import jakarta.persistence.*;
import lombok.*;
import my.mma.fighter.entity.BaseEntity;

@Table(name = "users")
@Builder
@AllArgsConstructor
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String email;

    // 'google'/'naver'/'kakao' + 해당 도메인으로부터 발급하는 아이디(번호)
    private String username;

    @Column(unique = true)
    private String nickname;

    private String password;

    private String role;

    private int point;

    private String fcmToken;

    public void updateEmail(String email){
        this.email = email;
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

    public void updatePassword(String password){
        this.password = password;
    }

    public void updatePoint(int point){
        this.point = point;
    }

    public void updateFcmToken(String fcmToken){
        this.fcmToken = fcmToken;
    }


}