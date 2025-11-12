package my.mma.fixture.entity.fightevent;

import my.mma.event.entity.FightEvent;
import my.mma.event.entity.property.CardStartDateTimeInfo;

import java.time.LocalDate;
import java.time.LocalTime;

import static my.mma.fixture.entity.fighterfightevent.FighterFightEventFixture.createUpcomingFfeWithId;

public class FightEventFixture {

    public static final int total_card_count = 18;

    public static FightEvent createUpcomingFightEventWithId(int id) {
        FightEvent fightEvent = getFightEventWithoutFfeWithNum(id);
        for (int i = 0; i < total_card_count; i++) {
            fightEvent.getFighterFightEvents().add(createUpcomingFfeWithId(fightEvent, i));
        }
        return fightEvent;
    }

    private static FightEvent getFightEventWithoutFfeWithNum(int id) {
        return FightEvent.builder()
                .id((long) id)
                .name("UFC-" + id)
                .eventDate(LocalDate.of(2000, 1, 1))
                .completed(false)
                .location("location-" + id)
                .mainCardCnt(total_card_count / 3)
                .mainCardDateTimeInfo(createCardStartDateTimeInfo())
                .prelimCardCnt(total_card_count / 3)
                .prelimCardDateTimeInfo(createCardStartDateTimeInfo())
                .earlyCardCnt(total_card_count / 3)
                .earlyCardDateTimeInfo(createCardStartDateTimeInfo())
                .build();
    }

    private static CardStartDateTimeInfo createCardStartDateTimeInfo() {
        return CardStartDateTimeInfo.builder()
                .date(LocalDate.of(2000, 1, 1))
                .time(LocalTime.MIDNIGHT)
                .build();
    }

}
