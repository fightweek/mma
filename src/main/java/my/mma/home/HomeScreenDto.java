package my.mma.home;

import lombok.*;
import my.mma.fightevent.dto.CardStartDateTimeInfoDto;
import my.mma.fightevent.dto.StreamFightEventDto;

// Response
@Builder
public record HomeScreenDto(String eventName, CardStartDateTimeInfoDto mainCardDateTimeInfo,
                            String winnerBodyUrl, String loserBodyUrl,
                            String winnerName, String loserName,
                            String fightWeight, boolean title, boolean now) {

    public static HomeScreenDto toDto(StreamFightEventDto sfe){
        return HomeScreenDto.builder()
                .eventName(sfe.getName())
                .mainCardDateTimeInfo(sfe.getMainCardDateTimeInfo())
                .winnerName(sfe.getFighterFightEvents().get(0).getWinner().getName())
                .winnerBodyUrl(sfe.getFighterFightEvents().get(0).getWinner().getBodyUrl())
                .loserBodyUrl(sfe.getFighterFightEvents().get(0).getLoser().getBodyUrl())
                .loserName(sfe.getFighterFightEvents().get(0).getLoser().getName())
                .fightWeight(sfe.getFighterFightEvents().get(0).getFightWeight())
                .title(sfe.getFighterFightEvents().get(0).isTitle())
                .now(sfe.isNow())
                .build();
    }

}
