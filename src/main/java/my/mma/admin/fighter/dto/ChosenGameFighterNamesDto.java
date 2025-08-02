package my.mma.admin.fighter.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChosenGameFighterNamesDto {

    private List<String> names;

}
