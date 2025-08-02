package my.mma.event.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FighterFightEvent;
import my.mma.fighter.dto.FighterDto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static my.mma.event.dto.FightEventDto.*;

/**
 * 크롤링 시 사용되지 않음 (admin의 실시간 이벤트 및 주기적인 upcoming, previous event update용 CrawlerDto와 무관)
 * 오직 클라이언트와 이벤트 정보 송수신 시 사용되는 dto
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class FightEventDto extends IFightEventDto<FighterFightEventDto>{

    private Long id;

    private boolean upcoming;

    private boolean alert;

    public static FightEventDto toDto(FightEvent fightEvent) {
        return FightEventDto.builder()
                .id(fightEvent.getId())
                .date(fightEvent.getEventDate())
                .mainCardDateTimeInfo(fightEvent.getMainCardDateTimeInfo() != null ?
                        CardStartDateTimeInfoDto.toDto(fightEvent.getMainCardDateTimeInfo()) : null)
                .prelimCardDateTimeInfo(fightEvent.getPrelimCardDateTimeInfo() != null ?
                        CardStartDateTimeInfoDto.toDto(fightEvent.getPrelimCardDateTimeInfo()) : null)
                .earlyCardDateTimeInfo(fightEvent.getEarlyCardDateTimeInfo() != null ?
                        CardStartDateTimeInfoDto.toDto(fightEvent.getEarlyCardDateTimeInfo()) : null)
                .mainCardCnt(fightEvent.getMainCardCnt())
                .prelimCardCnt(fightEvent.getPrelimCardCnt())
                .earlyCardCnt(fightEvent.getEarlyCardCnt())
                .location(fightEvent.getLocation())
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
    @SuperBuilder
    public static class FighterFightEventDto extends IFighterFightEvent<FighterDto>{

        private Long eventId;
        private String eventName;
        private LocalDate eventDate;

        public static FighterFightEventDto toDto(FighterFightEvent fighterFightEvent) {
            return FighterFightEventDto.builder()
                    .id(fighterFightEvent.getId())
                    .eventId(fighterFightEvent.getFightEvent().getId())
                    .eventName(fighterFightEvent.getFightEvent().getName())
                    .eventDate(fighterFightEvent.getFightEvent().getEventDate())
                    .fightWeight(fighterFightEvent.getFightWeight())
                    .result(fighterFightEvent.getFightResult() != null ? FightResultDto.toDto(fighterFightEvent.getFightResult()) : null)
                    .winner(FighterDto.toDto(fighterFightEvent.getWinner()))
                    .loser(FighterDto.toDto(fighterFightEvent.getLoser()))
                    .title(fighterFightEvent.isTitle())
                    .build();
        }

    }

}
