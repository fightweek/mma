package my.mma.event.dto;

import lombok.*;
import my.mma.event.entity.property.FightResult;
import my.mma.event.entity.property.WinMethod;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FightResultDto {

    private String winMethod;

    private int round;

    private String endTime;

    private String description;

    public static FightResult toFightResult(FightResultDto resultDto) {
        String[] timeParts = resultDto.getEndTime().split(":");
        return FightResult.builder()
                .winMethod(
                        resultDto.getWinMethod().contains("DEC") ? WinMethod.valueOf(resultDto.getWinMethod()) :
                                (resultDto.getWinMethod().contains("SUB") ? WinMethod.SUB :
                                        (resultDto.getWinMethod().contains("KO") ? WinMethod.KO_TKO : WinMethod.NC))
                )
                .winDescription(resultDto.getWinMethod().contains("SUB") ? resultDto.getWinMethod().split("_")[1] : null)
                .round(resultDto.getRound())
                .endTime(LocalTime.of(0,Integer.parseInt(timeParts[0],Integer.parseInt(timeParts[1]))))
                .build();
    }

    public static FightResultDto toDto(FightResult result){
        return FightResultDto.builder()
                .endTime(result.getEndTime().toString())
                .round(result.getRound())
                .winMethod(result.getWinMethod().name())
                .description(result.getWinDescription())
                .build();
    }

}
