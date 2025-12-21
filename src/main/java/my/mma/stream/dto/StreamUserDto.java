package my.mma.stream.dto;

import lombok.*;

@Builder
public record StreamUserDto(String nickname, long id, int point) {

}
