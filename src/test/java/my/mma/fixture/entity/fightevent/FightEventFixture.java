package my.mma.fixture.entity.fightevent;

import my.mma.fightevent.entity.FightEvent;
import my.mma.fightevent.entity.property.CardStartDateTimeInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static my.mma.fixture.entity.fighterfightevent.FighterFightEventFixture.createUpcomingFfeWithId;

public class FightEventFixture {

    private static final int TOTAL_CARD_COUNT = 12;

    public static FightEvent createUpcomingFightEventWithId(int id) {
        FightEvent fightEvent = getFightEventWithoutFfeWithNum(id);
        for (int i = 0; i < TOTAL_CARD_COUNT; i++) {
            fightEvent.getFighterFightEvents().add(createUpcomingFfeWithId(fightEvent, i));
        }
        return fightEvent;
    }

    private static FightEvent getFightEventWithoutFfeWithNum(int id) {
        LocalDateTime main = LocalDateTime.of(2000,1,1,10,0,0);
        LocalDateTime prelim = main.plusHours(4);
        LocalDateTime early = prelim.plusHours(4);

        return FightEvent.builder()
                .id((long) id)
                .name("UFC-" + id)
                .eventDate(LocalDate.of(2000, 1, 1))
                .completed(false)
                .location("location-" + id)
                .mainCardCnt(TOTAL_CARD_COUNT / 2)
                .mainCardDateTimeInfo(createCardStartDateTimeInfoWithLocalDateTime(main))
                .prelimCardCnt(TOTAL_CARD_COUNT / 3)
                .prelimCardDateTimeInfo(createCardStartDateTimeInfoWithLocalDateTime(prelim))
                .earlyCardCnt(TOTAL_CARD_COUNT / 6)
                .earlyCardDateTimeInfo(createCardStartDateTimeInfoWithLocalDateTime(early))
                .build();
    }

    private static CardStartDateTimeInfo createCardStartDateTimeInfoWithLocalDateTime(LocalDateTime localDateTime) {
        return CardStartDateTimeInfo.builder()
                .date(localDateTime.toLocalDate())
                .time(localDateTime.toLocalTime())
                .build();
    }

}
