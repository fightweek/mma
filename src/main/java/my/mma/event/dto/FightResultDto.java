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

    private WinMethod winMethod;

    private int round;

    private String endTime;

    private String description;

    private boolean draw;

    private boolean nc;

    public static FightResultDto toDto(FightResult result){
        return FightResultDto.builder()
                .endTime(result.getEndTime().toString())
                .round(result.getRound())
                .winMethod(result.getWinMethod())
                .description(result.getWinDescription())
                .draw(result.isDraw())
                .nc(result.isNc())
                .build();
    }

}
