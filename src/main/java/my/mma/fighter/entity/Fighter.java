package my.mma.fighter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.File;
import java.time.LocalDate;

@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Fighter extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fighter_id")
    private Long id;

    private String name;

    private String nickname;

    private int height;

    private Double weight;

    @Embedded
    private FightRecord fightRecord;

    private Integer ranking;

    private LocalDate birthday;

    private int reach;

    public void updateFightRecord(String[] newFightRecord){
        this.fightRecord = FightRecord.toFightRecord(newFightRecord);
    }

    public void updateRanking(Integer ranking){
        this.ranking = ranking;
    }
}