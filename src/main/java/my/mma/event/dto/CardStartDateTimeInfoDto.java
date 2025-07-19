package my.mma.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import my.mma.event.entity.property.CardStartDateTimeInfo;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardStartDateTimeInfoDto {

    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;

    public CardStartDateTimeInfo toEntity(){
        return CardStartDateTimeInfo.builder()
                .date(this.date)
                .time(this.time)
                .build();
    }

    public static CardStartDateTimeInfoDto toDto(CardStartDateTimeInfo info){
        return CardStartDateTimeInfoDto.builder()
                .date(info.getDate())
                .time(info.getTime())
                .build();
    }

}