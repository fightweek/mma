package my.mma.fightevent.dto.abs;

import lombok.*;
import lombok.experimental.SuperBuilder;
import my.mma.fightevent.dto.CardStartDateTimeInfoDto;
import my.mma.fighter.dto.IFighterDto;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public abstract class IFightEventDto<T extends IFighterFightEvent<? extends IFighterDto>> {

    protected long id;

    protected LocalDate date;

    protected CardStartDateTimeInfoDto mainCardDateTimeInfo;

    protected CardStartDateTimeInfoDto prelimCardDateTimeInfo;

    protected CardStartDateTimeInfoDto earlyCardDateTimeInfo;

    protected Integer mainCardCnt;

    protected Integer prelimCardCnt;

    protected Integer earlyCardCnt;

    protected String location;

    protected String name;

    protected List<T> fighterFightEvents;

}
