package my.mma.fighter.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalTime;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FightResult {

    private String winnerName;

    private String loserName;

    @Enumerated(EnumType.STRING)
    private WinMethod winMethod;

    private String winDescription;

    private int round;

    private LocalTime fightEndTime;

}
