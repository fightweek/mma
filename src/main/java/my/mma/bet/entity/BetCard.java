package my.mma.bet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FighterFightEvent;
import my.mma.fighter.entity.BaseEntity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class BetCard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "bet_card_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "bet_id")
    private Bet bet;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "fighter_fight_event_id")
    private FighterFightEvent fighterFightEvent;

    @Embedded
    private BetPrediction prediction;

    private Boolean succeed;

    private int seedPoint;

    protected void addBet(Bet bet){
        this.bet = bet;
    }

}
