package my.mma.fighter.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FighterRankingDto {

    @JsonProperty("rankers")
    private List<RankerDto> rankerDtos;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class RankerDto {

        private String ranking;

        @JsonProperty("name")
        private String rankerName;

        private String category;

    }

    @Getter
    @RequiredArgsConstructor
    public enum RankingCategory{

        MENS_POUND_FOR_POUND_TOP_RANK("남성 P4P"),
        FLYWEIGHT("플라이급"),
        BANTAMWEIGHT("밴텀급"),
        FEATHERWEIGHT("페더급"),
        LIGHTWEIGHT("라이트급"),
        WELTERWEIGHT("웰터급"),
        MIDDLEWEIGHT("미들급"),
        LIGHT_HEAVYWEIGHT("라이트헤비급"),
        HEAVYWEIGHT("헤비급"),
        WOMENS_POUND_FOR_POUND_TOP_RANK("여성 P4P"),
        WOMENS_STRAWWEIGHT("여성 스트로급"),
        WOMENS_FLYWEIGHT("여성 플라이급"),
        WOMENS_BANTAMWEIGHT("여성 벤텀급");

        private final String description;

    }

}
