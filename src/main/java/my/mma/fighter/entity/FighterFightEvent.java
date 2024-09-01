package my.mma.fighter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FighterFightEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fighter_fight_event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fighter1_id")
    private Fighter fighter1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fighter2_id")
    private Fighter fighter2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fight_event_id")
    private FightEvent fightEvent;

    private Boolean isEnded;

    private String winnerName;

    private String looserName;

    public void addFightEvent(FightEvent fightEvent){
        this.fightEvent = fightEvent;
    }

}