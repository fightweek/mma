package my.mma.event.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import my.mma.event.dto.abs.IFightEventDto;
import my.mma.event.dto.abs.IFighterFightEvent;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FighterFightEvent;
import my.mma.fighter.dto.IFighterDto;
import my.mma.fighter.entity.Fighter;

import java.time.LocalDate;

import static my.mma.event.dto.StreamFightEventDto.*;
import static my.mma.event.dto.StreamFighterFightEventStatus.*;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class StreamFightEventDto extends IFightEventDto<StreamFighterFightEventDto> {

    private LocalDate date;

    private boolean now;

    public static StreamFightEventDto toDto(FightEvent fightEvent) {
        return StreamFightEventDto.builder()
                .id(fightEvent.getId())
                .date(fightEvent.getEventDate())
                .mainCardDateTimeInfo(fightEvent.getMainCardDateTimeInfo() != null ?
                        CardStartDateTimeInfoDto.toDto(fightEvent.getMainCardDateTimeInfo()) : null)
                .prelimCardDateTimeInfo(fightEvent.getPrelimCardDateTimeInfo() != null ?
                        CardStartDateTimeInfoDto.toDto(fightEvent.getPrelimCardDateTimeInfo()) : null)
                .earlyCardDateTimeInfo(fightEvent.getEarlyCardDateTimeInfo() != null ?
                        CardStartDateTimeInfoDto.toDto(fightEvent.getEarlyCardDateTimeInfo()) : null)
                .mainCardCnt(fightEvent.getMainCardCnt() != null ? fightEvent.getMainCardCnt() : null)
                .prelimCardCnt(fightEvent.getPrelimCardCnt() != null ? fightEvent.getPrelimCardCnt() : null)
                .earlyCardCnt(fightEvent.getEarlyCardCnt() != null ? fightEvent.getEarlyCardCnt() : null)
                .location(fightEvent.getLocation())
                .name(fightEvent.getName())
                .now(false)
                .fighterFightEvents(fightEvent.getFighterFightEvents().stream().map(
                        ffe -> StreamFighterFightEventDto.toDto(ffe, UPCOMING)
                ).toList())
                .build();
    }

    @Getter
    @Setter
    @SuperBuilder
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StreamFighterFightEventDto extends IFighterFightEvent<FighterFightEventCardFighterDto> {

        private StreamFighterFightEventStatus status;

        private double winnerVoteRate;

        private double loserVoteRate;

        // for streaming (upcoming) event -> result is null
        public static StreamFighterFightEventDto toDto(FighterFightEvent ffe, StreamFighterFightEventStatus status) {
            return StreamFighterFightEventDto.builder()
                    .id(ffe.getId())
                    .status(status)
                    .eventName(ffe.getFightEvent().getName())
                    .fightWeight(ffe.getFightWeight())
                    .winner(FighterFightEventCardFighterDto.toDto(ffe.getWinner()))
                    .loser(FighterFightEventCardFighterDto.toDto(ffe.getLoser()))
                    .result(null)
                    .title(ffe.isTitle())
                    .build();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @SuperBuilder
    public static class FighterFightEventCardFighterDto extends IFighterDto {

        private int reach;

        private LocalDate birthday;

        private int height;

        private String bodyUrl;

        private Double weight;

        public static FighterFightEventCardFighterDto toDto(Fighter fighter) {
            return FighterFightEventCardFighterDto.builder()
                    .id(fighter.getId())
                    .name(fighter.getName())
                    .nickname(fighter.getNickname())
                    .height(fighter.getHeight())
                    .weight(fighter.getWeight())
                    .reach(fighter.getReach())
                    .record(fighter.getFightRecord())
                    .ranking(fighter.getRanking())
                    .birthday(fighter.getBirthday())
                    .build();
        }
    }

}
