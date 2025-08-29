package my.mma.game.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class ImageGameQuestions {

    private List<ImageGameQuestionDto> gameQuestions = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor
    @Builder
    public static class ImageGameQuestionDto{

        private String name;

        private String answerImgUrl;

        @Builder.Default
        private List<String> wrongSelection = new ArrayList<>();

    }

}
