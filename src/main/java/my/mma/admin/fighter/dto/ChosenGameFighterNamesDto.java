package my.mma.admin.fighter.dto;

import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class ChosenGameFighterNamesDto {

    private Set<String> names = new HashSet<>();

}
