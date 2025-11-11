package my.mma.fixture.fighterfightevent;

import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FighterFightEvent;

import static my.mma.fixture.fighter.FighterFixture.*;

public class FighterFightEventFixture {

    public static FighterFightEvent createUpcomingFfeWithId(FightEvent fightEvent, int id){
        return FighterFightEvent.builder()
                .id((long) id)
                .fightEvent(fightEvent)
                .winner(createFighter())
                .loser(createFighter())
                .title(false)
                .fightResult(null)
                .build();
    }

    public static FighterFightEvent createUpcomingFfe(FightEvent fightEvent){
        return FighterFightEvent.builder()
                .id(1L)
                .fightEvent(fightEvent)
                .winner(createFighter())
                .loser(createFighter())
                .title(false)
                .fightResult(null)
                .build();
    }

}
