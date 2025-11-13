package my.mma.fightevent.dto.abs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import my.mma.fightevent.dto.FightResultDto;
import my.mma.fighter.dto.IFighterDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class IFighterFightEvent<T extends IFighterDto> {

    private String eventName;

    protected Long id;

    protected String fightWeight;

    protected T winner;

    protected T loser;

    protected FightResultDto result;

    protected boolean title;

}
