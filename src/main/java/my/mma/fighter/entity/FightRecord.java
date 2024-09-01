package my.mma.fighter.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FightRecord {

    private Integer win;
    private Integer draw;
    private Integer loss;

}
