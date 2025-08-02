package my.mma.admin.event.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.CardStartDateTimeInfoDto;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.property.FightResult;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.entity.property.WinMethod;
import my.mma.fighter.entity.*;
import my.mma.global.utils.ModifyUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static my.mma.global.utils.ModifyUtils.toKg;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawlerDto {

    private List<FighterCrawlerDto> fighters;

    private List<EventCrawlerDto> events;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    @Slf4j
    public static class FighterCrawlerDto{

        private String name;

        private String record;

        private String weight;

        private String height;

        private String nickname;

        private String reach;

        private String birthday;

        public Fighter toEntity(){
            String[] split_record = record.split("-");
            return Fighter.builder()
                    .weight(weight != null ? toKg(this.weight) : null)
                    .height(toCentimeter(this.height))
                    .name(this.getName())
                    .fightRecord(
                            FightRecord.builder()
                                    .win(Integer.parseInt(split_record[0]))
                                    .loss(Integer.parseInt(split_record[1]))
                                    .draw(((int) split_record[2].charAt(0)) - 48)
                                    .build()
                    )
                    .nickname(this.getNickname())
                    .reach(this.reach.contains("-") ? 0 : Integer.parseInt(this.reach))
                    .birthday(LocalDate.parse(this.birthday,DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)))
                    .build();
        }

        public int toCentimeter(String footInch) {
            try {
                String[] split = footInch.split("'\\s*");
                if (split.length != 2)
                    return 0;
                return (int) ((Integer.parseInt(split[0]) * 12 + Integer.parseInt(split[1])) * 2.54 + 0.5);
            }catch (Exception e){
                log.info("e = ",e);
                return 0;
            }
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

        @JsonProperty("main_card_info")
        private CardStartDateTimeInfoDto mainCardDateTimeInfo;

        @JsonProperty("prelim_card_info")
        private CardStartDateTimeInfoDto prelimCardDateTimeInfo;

        @JsonProperty("early_card_info")
        private CardStartDateTimeInfoDto earlyCardDateTimeInfo;

        @JsonProperty("main_card_cnt")
        private Integer mainCardCnt;

        @JsonProperty("prelim_card_cnt")
        private Integer prelimCardCnt;

        @JsonProperty("early_card_cnt")
        private Integer earlyCardCnt;

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

            private boolean title;

            private String round;

            @JsonProperty("fight_time")
            private String fightTime;

            private boolean draw;

            private boolean nc;

            public FightResult buildFightResult() {
                return FightResult.builder()
                        .winMethod(
                                method.contains("DEC") ? WinMethod.valueOf(this.method) :
                                        (method.contains("SUB") ? WinMethod.SUB :
                                                (method.contains("KO") ? WinMethod.KO_TKO : WinMethod.DQ))
                        )
                        .winDescription(method.contains("SUB") ? method.split("_")[1] : null)
                        .endTime(LocalTime.parse(this.fightTime, DateTimeFormatter.ofPattern("H:mm")))
                        .round(Integer.parseInt(this.round))
                        .build();
            }

            public FighterFightEvent toEntity(Fighter winner, Fighter loser){
                return FighterFightEvent.builder()
                        .title(title)
                        .winner(winner)
                        .loser(loser)
                        .fightWeight(this.fightWeight)
                        .fightResult(null)
                        .build();
            }

        }

        // 단순 이벤트명만 비교하는 용도로 사용되는 빌더
        public FightEvent toEntityForEventName(){
            return FightEvent.builder()
                    .location(location)
                    .eventDate(LocalDate.parse(this.eventDate,DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)))
                    .name(this.eventName)
                    .build();
        }

        public FightEvent toEntityUpcomingEvent(){
            return FightEvent.builder()
                    .mainCardDateTimeInfo(this.mainCardDateTimeInfo != null ? this.mainCardDateTimeInfo.toEntity() : null)
                    .prelimCardDateTimeInfo(this.mainCardDateTimeInfo != null ? this.prelimCardDateTimeInfo.toEntity() : null)
                    .earlyCardDateTimeInfo(this.earlyCardDateTimeInfo != null ? this.earlyCardDateTimeInfo.toEntity() : null)
                    .mainCardCnt(this.mainCardCnt)
                    .prelimCardCnt(this.prelimCardCnt)
                    .earlyCardCnt(this.earlyCardCnt)
                    .completed(false)
                    .location(location)
                    .eventDate(LocalDate.parse(this.eventDate,DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)))
                    .name(eventName)
                    .build();
        }

    }

}
