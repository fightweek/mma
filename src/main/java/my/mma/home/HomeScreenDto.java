package my.mma.home;

import lombok.*;
import my.mma.event.dto.CardStartDateTimeInfoDto;
import my.mma.event.dto.StreamFightEventDto;

// Response
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HomeScreenDto {

    private String eventName;
    private CardStartDateTimeInfoDto mainCardDateTimeInfo;
    private String winnerBodyUrl;
    private String loserBodyUrl;
    private String winnerName;
    private String loserName;
    private String fightWeight;
    private boolean title;
    private boolean now;

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
