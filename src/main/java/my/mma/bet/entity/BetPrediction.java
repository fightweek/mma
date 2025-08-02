package my.mma.bet.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;
import my.mma.event.entity.property.WinMethod;

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

    private long winnerId;

    private long loserId;

    private Integer winRound;

    @Enumerated(STRING)
    private WinMethod winMethod;

}
