package my.mma.event.dto;

import lombok.*;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FightResult;
import my.mma.event.entity.FighterFightEvent;
import my.mma.fighter.dto.FighterDto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class FightEventDto {

    private Long id;

    private LocalDate date;

    private String location;

    private String name;

    private List<FighterFightEventDto> fighterFightEvents;

    private boolean upcoming;

    public static FightEventDto toDto(FightEvent fightEvent) {
        return FightEventDto.builder()
                .id(fightEvent.getId())
                .date(fightEvent.getEventDate())
                .location(fightEvent.getEventLocation())
                .name(fightEvent.getName())
                .upcoming(!fightEvent.isCompleted())
                .fighterFightEvents(fightEvent.getFighterFightEvents().stream().map(
                        FighterFightEventDto::toDto
                ).collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class FighterFightEventDto {

        private String eventName;
        private String fightWeight;
        private FighterDto winner;
        private FighterDto loser;
        private FightResult result;

        public static FighterFightEventDto toDto(FighterFightEvent fighterFightEvent) {
            return FighterFightEventDto.builder()
                    .eventName(fighterFightEvent.getFightEvent().getName())
                    .fightWeight(fighterFightEvent.getFightWeight())
                    .result(fighterFightEvent.getFightResult())
                    .winner(FighterDto.toDto(fighterFightEvent.getWinner()))
                    .loser(FighterDto.toDto(fighterFightEvent.getLoser()))
                    .build();
        }

    }

}
