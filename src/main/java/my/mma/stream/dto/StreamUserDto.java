package my.mma.stream.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamUserDto {

    private String nickname;
    private long id;
    private int point;

}
