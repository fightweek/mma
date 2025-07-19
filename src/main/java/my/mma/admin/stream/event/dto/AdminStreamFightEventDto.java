package my.mma.admin.stream.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import my.mma.event.dto.FightResultDto;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminStreamFightEventDto {

    private String name;

    private List<AdminStreamFighterFightEventDto> streamFighterFightEvents;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AdminStreamFighterFightEventDto{

        @JsonProperty("fight_weight")
        private String fightWeight;

        @JsonProperty("winner_name")
        private String winnerName;

        @JsonProperty("loser_name")
        private String loserName;

        @JsonProperty("result")
        private FightResultDto result;

    }

}
