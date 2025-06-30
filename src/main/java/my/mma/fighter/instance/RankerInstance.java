package my.mma.fighter.instance;

import lombok.Getter;
import lombok.Setter;
import my.mma.admin.fighter.dto.FighterRankingDto.RankerDto;

import java.util.ArrayList;
import java.util.List;

public class RankerInstance {

    @Getter
    @Setter
    private static List<RankerDto> rankers = new ArrayList<>();

}
