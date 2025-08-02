package my.mma.game.dto;

import lombok.*;
import my.mma.fighter.entity.FightRecord;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class GameQuestionsDto {

    private List<GameQuestionDto> gameQuestions;

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class GameQuestionDto {

        private GameCategory gameCategory;

        // name은 어떤 category 든지 무조간 들어가야 됨
        private String name;

        private String nickname;

        private Integer ranking;
        private String rankingCategory; // pfp / 체급

        private String bodyUrl;

        private String headshotUrl;

        private FightRecord fightRecord;

        private List<String> namesForSelection;

    }

}
