package my.mma.fighter.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import my.mma.event.dto.IFighterDto;
import my.mma.fighter.entity.FightRecord;
import my.mma.fighter.entity.Fighter;

@Getter
@Setter
@SuperBuilder
public class FighterDto extends IFighterDto {

    public static FighterDto toDto(Fighter fighter){
        return FighterDto.builder()
                .id(fighter.getId())
                .name(fighter.getName())
                .ranking(fighter.getRanking())
                .nickname(fighter.getNickname())
                .record(fighter.getFightRecord())
                .weight(fighter.getWeight())
                .build();
    }

}
