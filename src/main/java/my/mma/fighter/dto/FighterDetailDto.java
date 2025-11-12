package my.mma.fighter.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import my.mma.event.dto.FightEventDto.FighterFightEventDto;
import my.mma.fighter.entity.Fighter;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class FighterDetailDto extends FighterDto{

    private int height;
    protected Double weight;
    private LocalDate birthday;
    private int reach;
    private String nation;
    private boolean alert;
    private String bodyUrl;
    private List<FighterFightEventDto> fighterFightEvents;

    public static FighterDetailDto toDto(Fighter fighter, List<FighterFightEventDto> fighterFightEvents, String bodyUrl, boolean alert) {
        return FighterDetailDto.builder()
                .id(fighter.getId())
                .name(fighter.getName())
                .nickname(fighter.getNickname())
                .ranking(fighter.getRanking())
                .record(fighter.getFightRecord())
                .weight(fighter.getWeight())
                .height(fighter.getHeight())
                .birthday(fighter.getBirthday())
                .reach(fighter.getReach())
                .nation(null)
                .fighterFightEvents(fighterFightEvents)
                .bodyUrl(bodyUrl)
                .alert(alert)
                .build();
    }

}
