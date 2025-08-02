package my.mma.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import my.mma.fighter.dto.FighterDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class IFighterFightEvent<T extends IFighterDto> {

    protected Long id;

    protected String fightWeight;
    
    protected T winner;
    
    protected T loser;
    
    protected FightResultDto result;

    protected boolean title;

}
