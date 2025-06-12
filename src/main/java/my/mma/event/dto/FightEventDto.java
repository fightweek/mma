package my.mma.event.dto;

import lombok.*;
import my.mma.event.entity.FightEvent;
import my.mma.fighter.entity.FightRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FightEventDto {

    private Long id;

    private LocalDate date;

    private String location;

    private String name;

    private List<FighterFightEventDto> fighterFightEvents;

    public static FightEventDto toDto(FightEvent fightEvent) {
        return FightEventDto.builder()
                .id(fightEvent.getId())
                .date(fightEvent.getEventDate())
                .location(fightEvent.getEventLocation())
                .name(fightEvent.getEventName())
                .fighterFightEvents(fightEvent.getFighterFightEvents().stream().map(
                        fighterFightEvent -> FighterFightEventDto.builder()
                                .fightWeight(fighterFightEvent.getFightWeight())
                                .winnerName(fighterFightEvent.getWinner().getName())
                                .winnerRecord(fighterFightEvent.getWinner().getFightRecord())
                                .loserName(fighterFightEvent.getLoser().getName())
                                .loserRecord(fighterFightEvent.getLoser().getFightRecord())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class FighterFightEventDto {

        private String fightWeight;

        private String winnerName;

        private FightRecord winnerRecord;

        private String loserName;

        private FightRecord loserRecord;

        private String winnerImgPresignedUrl;

        private String loserImgPresignedUrl;

    }

}
