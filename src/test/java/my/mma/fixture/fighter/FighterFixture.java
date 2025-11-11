package my.mma.fixture.fighter;

import my.mma.fighter.entity.FightRecord;
import my.mma.fighter.entity.Fighter;

import java.time.LocalDate;
import java.util.Random;

public class FighterFixture {

    public static Fighter createFighter() {
        return Fighter.builder()
                .id(1L)
                .name("name-" + 1)
                .nickname("nickname-" + 1)
                .height(180)
                .weight(155.5)
                .reach(100)
                .ranking(new Random().nextInt(15))
                .birthday(LocalDate.of(1990, 1, 1))
                .fightRecord(FightRecord.builder()
                        .win(10)
                        .draw(0)
                        .loss(1)
                        .build())
                .build();
    }

    public static Fighter createFighterWithNumber(int num) {
        return Fighter.builder()
                .id((long) num)
                .name("name-" + num)
                .nickname("nickname-" + num)
                .height(180)
                .weight(155.5)
                .reach(100)
                .ranking(new Random().nextInt(15))
                .birthday(LocalDate.of(1990, 1, 1))
                .fightRecord(FightRecord.builder()
                        .win(num + 10)
                        .draw(num)
                        .loss(num + 1)
                        .build())
                .build();
    }

}
