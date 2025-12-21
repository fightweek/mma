package my.mma.admin.fighter.dto;

import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class ChosenGameFighterNamesDto {

    private final Set<String> names = new HashSet<>();

}
