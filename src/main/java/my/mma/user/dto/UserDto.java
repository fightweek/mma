package my.mma.user.dto;

import lombok.*;
import my.mma.user.entity.User;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;

    private String nickname;

    private String email;

    private String role;

    private int point;

    public static UserDto toDto(User user){
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .point(user.getPoint())
                .build();
    }

}
