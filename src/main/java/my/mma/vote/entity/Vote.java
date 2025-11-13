package my.mma.vote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.mma.fightevent.entity.FighterFightEvent;
import my.mma.user.entity.User;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(
        name = "vote",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "fighter_fight_event_id"})
)
public class Vote {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "vote_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private boolean succeed;

    private Long winnerId;

    private Long loserId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "fighter_fight_event_id")
    private FighterFightEvent fighterFightEvent;

    public void swapPrediction(){
        Long temp = winnerId;
        winnerId = loserId;
        loserId = temp;
    }

}
