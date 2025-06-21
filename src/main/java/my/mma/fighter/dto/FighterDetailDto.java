package my.mma.fighter.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import my.mma.event.dto.FightEventDto.FighterFightEventDto;
import my.mma.fighter.entity.Fighter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@ToString
public class FighterDetailDto extends FighterDto{

    private String height;
    private LocalDate birthday;
    private int reach;
    private String nation;
    private List<FighterFightEventDto> fighterFightEvents;

    public static FighterDetailDto toDto(Fighter fighter, List<FighterFightEventDto> fighterFightEvents, String imgPresignedUrl) {
        FighterDetailDto detailDto = FighterDetailDto.builder()
                .id(fighter.getId())
                .name(fighter.getName())
                .ranking(fighter.getRanking())
                .record(fighter.getFightRecord())
                .weight(fighter.getWeight())
                .height(fighter.getHeight())
                .birthday(fighter.getBirthday())
                .reach(fighter.getReach())
                .nation(null)
                .fighterFightEvents(fighterFightEvents)
                .imgPresignedUrl(imgPresignedUrl)
                .build();
        System.out.println("detailDto = " + detailDto);
        System.out.println("detailDto.getImgPresignedUrl() = " + detailDto.getImgPresignedUrl());
        return detailDto;
    }

}
