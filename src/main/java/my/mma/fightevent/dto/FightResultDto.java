package my.mma.fightevent.dto;

import lombok.*;
import my.mma.fightevent.entity.property.FightResult;
import my.mma.fightevent.entity.property.WinMethod;

import java.time.format.DateTimeFormatter;

@Builder
public record FightResultDto(WinMethod winMethod, int round, String endTime, String description,
                             boolean draw, boolean nc) {

    public static FightResultDto toDto(FightResult result){
        return FightResultDto.builder()
                .endTime(result.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .round(result.getRound())
                .winMethod(result.getWinMethod())
                .description(result.getWinDescription())
                .draw(result.isDraw())
                .nc(result.isNc())
                .build();
    }

}
