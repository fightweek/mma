package my.mma.fighter.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import my.mma.fighter.entity.FightRecord;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public abstract class IFighterDto {

    protected Long id;
    protected String name;
    protected String nickname;
    protected Integer ranking;
    protected FightRecord record;
    @Setter
    protected String headshotUrl;

}
