package my.mma.fighter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FightEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fight_event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_fighter1_id")
    private Fighter mainFighter1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_fighter2_id")
    private Fighter mainFighter2;

    @Column(unique = true)
    private LocalDate fightDate;

    @OneToMany(mappedBy = "fightEvent", cascade = CascadeType.ALL)
    private List<FighterFightEvent> fighterFightEvents;

    public void addFighterFightEvent(FighterFightEvent fighterFightEvent){
        fighterFightEvent.addFightEvent(this);
        this.fighterFightEvents.add(fighterFightEvent);
    }

}