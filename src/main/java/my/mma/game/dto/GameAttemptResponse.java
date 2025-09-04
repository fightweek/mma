package my.mma.game.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Builder
public class GameAttemptResponse {

    private int count;

    private int adCount;

}
