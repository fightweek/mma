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

    public static UserDto toDto(User user){
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

}
