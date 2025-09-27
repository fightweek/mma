package my.mma.event.entity;

import jakarta.persistence.*;
import lombok.*;
import my.mma.event.entity.property.FightResult;
import my.mma.fighter.entity.BaseEntity;
import my.mma.fighter.entity.Fighter;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FighterFightEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fighter_fight_event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Fighter winner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loser_id")
    private Fighter loser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fight_event_id")
    private FightEvent fightEvent;

    // ex) Bantamweight
    private String fightWeight;

    @Embedded
    private FightResult fightResult;

    private boolean title;

    protected void addFightEvent(FightEvent fightEvent){
        this.fightEvent = fightEvent;
    }

    public void updateFightResult(FightResult fightResult){
        this.fightResult = fightResult;
    }

    public void swapWinnerAndLoser(){
        Fighter temp = winner;
        winner = loser;
        loser = temp;
    }

    public void updateDrawAndNc(boolean draw, boolean nc){
        this.fightResult.setDraw(draw);
        this.fightResult.setNc(nc);
    }

}