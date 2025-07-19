package my.mma.fighter.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import my.mma.fighter.entity.FightRecord;
import my.mma.fighter.entity.Fighter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class FighterDto {

    private Long id;
    private String name;
    private String nickname;
    private Integer ranking;
    private FightRecord record;
    private String weight;
    private String headshotUrl;

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
