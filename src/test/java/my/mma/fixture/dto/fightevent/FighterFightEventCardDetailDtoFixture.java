package my.mma.fixture.dto.fightevent;

import my.mma.fighter.entity.FightRecord;
import my.mma.fightevent.dto.FighterFightEventCardDetailDto;
import my.mma.fightevent.dto.StreamFightEventDto.FighterFightEventCardFighterDto;

import java.time.LocalDate;

public class FighterFightEventCardDetailDtoFixture {

    public static FighterFightEventCardDetailDto createFighterFightEventCardDetailDto(){
        return FighterFightEventCardDetailDto.builder()
                .winner(createFighterFightEventCardFighterDto(1L))
                .loser(createFighterFightEventCardFighterDto(2L))
                .fightWeight("fight-weight")
                .build();
    }

    public static FighterFightEventCardFighterDto createFighterFightEventCardFighterDto(long id){
        return FighterFightEventCardFighterDto.builder()
                .id(id)
                .name("name-"+id)
                .nickname("nickname-"+id)
                .birthday(LocalDate.of(2000,10,10))
                .height(100)
                .weight(155.5)
                .reach(10)
                .record(FightRecord.builder()
                        .win(10)
                        .loss(1)
                        .draw(0)
                        .build())
                .build();
    }

}
