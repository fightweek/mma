package my.mma.bet.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;
import my.mma.fightevent.entity.property.WinMethod;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Setter
@Embeddable
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class BetPrediction {

    // 실제 승자 x, 내가 선택한 승자
    private String myWinnerName;

    // 실제 패자 x, 내가 선택한 패자
    private String myLoserName;

    private Integer winRound;

    private boolean draw;

    @Enumerated(STRING)
    private WinMethod winMethod;

}
