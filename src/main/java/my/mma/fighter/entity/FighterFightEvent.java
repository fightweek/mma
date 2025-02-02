package my.mma.fighter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FighterFightEvent extends BaseEntity{

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

    private String fightWeight;

    @Embedded
    private FightResult fightResult;

    public void addFightEvent(FightEvent fightEvent){
        this.fightEvent = fightEvent;
    }

}