package my.mma.event.dto;

import lombok.Builder;
import my.mma.event.entity.FightEvent;
import my.mma.fighter.entity.FightRecord;

@Builder
public record FightEventPaginationDto(long id, String name, String winnerName, String loserName,
                                      String winnerHeadshotUrl, String loserHeadshotUrl,
                                      FightRecord winnerRecord, FightRecord loserRecord) {

    public static FightEventPaginationDto toDto(FightEvent fightEvent, String winnerHeadshotUrl, String loserHeadshotUrl) {
        return FightEventPaginationDto.builder()
                .id(fightEvent.getId())
                .name(fightEvent.getName())
                .winnerName(fightEvent.getFighterFightEvents().get(0).getWinner().getName())
                .loserName(fightEvent.getFighterFightEvents().get(0).getLoser().getName())
                .winnerHeadshotUrl(winnerHeadshotUrl)
                .loserHeadshotUrl(loserHeadshotUrl)
                .winnerRecord(fightEvent.getFighterFightEvents().get(0).getWinner().getFightRecord())
                .loserRecord(fightEvent.getFighterFightEvents().get(0).getLoser().getFightRecord())
                .build();
    }
}
