package my.mma.game.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class GameResponse {

    private NameGameQuestions nameGameQuestions;

    private ImageGameQuestions imageGameQuestions;

}
