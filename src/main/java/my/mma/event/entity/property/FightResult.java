package my.mma.event.entity.property;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Enumerated(EnumType.STRING)
    @JsonProperty("method")
    private WinMethod winMethod;

    private String winDescription;

    private int round;

    private LocalTime endTime;

    // draw (both are winner)
    private boolean draw;

    // no contest (no winner & loser)
    private boolean nc;

}
