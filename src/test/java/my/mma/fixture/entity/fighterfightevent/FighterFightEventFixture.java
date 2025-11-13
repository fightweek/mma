package my.mma.fixture.entity.fighterfightevent;

import my.mma.fightevent.entity.FightEvent;
import my.mma.fightevent.entity.FighterFightEvent;
import my.mma.fixture.entity.fighter.FighterFixture;

public class FighterFightEventFixture {

    public static FighterFightEvent createUpcomingFfeWithId(FightEvent fightEvent, int id){
        return FighterFightEvent.builder()
                .id((long) id)
                .fightEvent(fightEvent)
                .winner(FighterFixture.createFighter())
                .loser(FighterFixture.createFighter())
                .title(false)
                .fightResult(null)
                .build();
    }

    public static FighterFightEvent createUpcomingFfe(FightEvent fightEvent){
        return FighterFightEvent.builder()
                .id(1L)
                .fightEvent(fightEvent)
                .winner(FighterFixture.createFighter())
                .loser(FighterFixture.createFighter())
                .title(false)
                .fightResult(null)
                .build();
    }

}
