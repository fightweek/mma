package my.mma.event.dto;

import lombok.*;
import my.mma.event.entity.property.FightResult;
import my.mma.event.entity.property.WinMethod;

import java.time.LocalTime;

@Builder
public record FightResultDto(WinMethod winMethod, int round, String endTime, String description,
                             boolean draw, boolean nc) {

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
