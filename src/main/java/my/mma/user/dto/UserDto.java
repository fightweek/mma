package my.mma.user.dto;

import lombok.*;
import my.mma.user.entity.User;

@Builder
public record UserDto(long id, String nickname, String email, String role, int point) {

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
