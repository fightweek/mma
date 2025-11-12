package my.mma.fixture.dto.fighter;

import my.mma.fighter.dto.FighterDetailDto;
import my.mma.fighter.dto.FighterDto;
import my.mma.fighter.entity.FightRecord;

import java.time.LocalDate;
import java.util.ArrayList;

public class FighterDtoFixture {

    public static FighterDto createFighterDto(Long fighterId, String headshotUrl) {
        return FighterDto.builder()
                .id(fighterId)
                .name("name-" + fighterId)
                .nickname("nickname-" + fighterId)
                .ranking(10)
                .record(FightRecord.builder()
                        .win(10)
                        .draw(0)
                        .loss(1)
                        .build())
                .headshotUrl(headshotUrl)
                .build();
    }

}
