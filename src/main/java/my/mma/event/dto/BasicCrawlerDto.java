package my.mma.event.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FightResult;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.entity.WinMethod;
import my.mma.fighter.entity.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class BasicCrawlerDto {

    private List<FighterCrawlerDto> fighters;

    private List<EventCrawlerDto> events;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class FighterCrawlerDto{

        @JsonProperty("name")
        private String fighterName;

        private String record;

        private String weight;

        private String height;

        private String nickname;

        private String birthday;

        public Fighter toEntity(){
            String[] split_record = record.split("-");
            return Fighter.builder()
                    .weight(this.weight)
                    .division(Fighter.get_division(this.weight))
                    .height(this.height)
                    .name(this.getFighterName())
                    .fightRecord(
                            FightRecord.builder()
                                    .win(Integer.parseInt(split_record[0]))
                                    .loss(Integer.parseInt(split_record[1]))
                                    .draw(((int) split_record[2].charAt(0)) - 48)
                                    .build()
                    )
                    .nickname(this.getNickname())
                    .birthday(LocalDate.parse(this.birthday,DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)))
                    .build();
        }

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class EventCrawlerDto {

        @JsonProperty("event_name")
        private String eventName;

        @JsonProperty("event_date")
        private String eventDate;

        private String location;

        private List<Card> cards;

        @Getter
        @Setter
        public static class Card {

            @JsonProperty("winner")
            private String winnerName;

            @JsonProperty("loser")
            private String loserName;

            private String method;

            @JsonProperty("fight_weight")
            private String fightWeight;

            private String round;

            @JsonProperty("fight_time")
            private String fightTime;

            public FighterFightEvent toEntityPrevEvent(Fighter winner, Fighter loser){
                return FighterFightEvent.builder()
                        .winner(winner)
                        .loser(loser)
                        .fightWeight(this.fightWeight)
                        .fightResult(
                                buildFightResult()
                        ).build();
            }

            public FightResult buildFightResult() {
                return FightResult.builder()
                        .winnerName(this.winnerName)
                        .loserName(this.loserName)
                        .winMethod(
                                method.contains("DEC") ? WinMethod.valueOf(this.method) :
                                        (method.contains("SUB") ? WinMethod.SUB :
                                                (method.contains("KO") ? WinMethod.KO_TKO : WinMethod.ELSE))
                        )
                        .winDescription(this.method)
                        .fightEndTime(LocalTime.parse(this.fightTime, DateTimeFormatter.ofPattern("H:mm")))
                        .round(Integer.parseInt(this.round))
                        .build();
            }

            public FighterFightEvent toEntityUpcomingEvent(Fighter winner, Fighter loser){
                return FighterFightEvent.builder()
                        .winner(winner)
                        .loser(loser)
                        .fightWeight(this.fightWeight)
                        .fightResult(null)
                        .build();
            }

        }

        public FightEvent toEntityPrevEvent(){
            return FightEvent.builder()
                    .completed(true)
                    .eventLocation(location)
                    .eventDate(LocalDate.parse(this.eventDate,DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)))
                    .eventName(this.eventName)
                    .build();
        }

        public FightEvent toEntityUpcomingEvent(){
            return FightEvent.builder()
                    .completed(false)
                    .eventLocation(location)
                    .eventDate(LocalDate.parse(this.eventDate,DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)))
                    .eventName(eventName)
                    .build();
        }

    }

}
