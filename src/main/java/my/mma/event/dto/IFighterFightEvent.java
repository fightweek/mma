package my.mma.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import my.mma.event.entity.property.FightResult;
import my.mma.fighter.dto.FighterDto;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class IFighterFightEvent<T extends FighterDto> {

    private String fightWeight;
    
    private T winner;
    
    private T loser;
    
    private FightResultDto result;

}
