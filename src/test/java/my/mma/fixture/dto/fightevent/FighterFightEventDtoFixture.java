package my.mma.fixture.dto.fightevent;

import my.mma.fightevent.dto.FightEventDto;
import my.mma.fightevent.dto.FightEventDto.FighterFightEventDto;

import static my.mma.fixture.dto.fighter.FighterDtoFixture.createFighterDto;

public class FighterFightEventDtoFixture {

    public static FighterFightEventDto createUpcomingFfeDtoWithId(FightEventDto fightEventDto, int id, String headshotUrl){
        return FighterFightEventDto.builder()
                .id((long) id)
                .eventName(fightEventDto.getName())
                .eventDate(fightEventDto.getDate())
                .fightWeight("fight-weight-"+id)
                .winner(createFighterDto((long)id,headshotUrl))
                .loser(createFighterDto((long)(id+1),headshotUrl))
                .title(false)
                .result(null)
                .build();
    }

}
