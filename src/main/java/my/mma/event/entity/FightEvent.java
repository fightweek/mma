package my.mma.event.entity;

import jakarta.persistence.*;
import lombok.*;
import my.mma.fighter.entity.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FightEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fight_event_id")
    private Long id;

    private LocalDate eventDate;

    private String eventLocation;

    private String name;

    private boolean completed; // true : 지난 / false : 차후

    private int score; //평점

    //빌더 패턴은 필드 값을 명시적으로 설정해야 하고, 초기값을 보장하지 않음 (빌더로 fightEvent 생성해도 빈 리스트 생성 보장 x)
    @OneToMany(mappedBy = "fightEvent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<FighterFightEvent> fighterFightEvents = new ArrayList<>();

    public void addFighterFightEvent(FighterFightEvent fighterFightEvent){
        fighterFightEvent.addFightEvent(this);
        this.fighterFightEvents.add(fighterFightEvent);
    }

}