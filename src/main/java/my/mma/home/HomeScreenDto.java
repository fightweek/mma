package my.mma.home;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import my.mma.event.dto.CardStartDateTimeInfoDto;
import my.mma.event.dto.StreamFightEventDto;

// Response
@Getter
@Setter
@Builder
public class HomeScreenDto {

    private String eventName;
    private CardStartDateTimeInfoDto mainCardDateTimeInfo;
    private String winnerBodyUrl;
    private String loserBodyUrl;
    private String winnerName;
    private String loserName;
    private boolean now;

    public static HomeScreenDto toDto(StreamFightEventDto sfe){
        return HomeScreenDto.builder()
                .eventName(sfe.getName())
                .mainCardDateTimeInfo(sfe.getMainCardDateTimeInfo())
                .winnerName(sfe.getFighterFightEvents().get(0).getWinner().getName())
                .loserName(sfe.getFighterFightEvents().get(0).getLoser().getName())
                .now(sfe.isNow())
                .build();
    }

}
