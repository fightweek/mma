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

    private String height;

    private String weight;

//    @Enumerated(EnumType.STRING)
//    private Division division;

    @Embedded
    private FightRecord fightRecord;

    private File picture;

//    @OneToMany(mappedBy = "fighter")
//    private List<FightHighlight> highlights = new ArrayList<>();

    private Integer ranking;

    private LocalDate birthday;

    private Boolean gender;

    private String status;

    private int reach;

//    public static Division get_division(String weight) {
//        int parsedWeight = Integer.parseInt(weight);
//        return switch (parsedWeight) {
//            case 115 -> Division.STRAWWEIGHT.getDescription();
//            case 125 -> Division.FLYWEIGHT;
//            case 135 -> Division.BANTAMWEIGHT;
//            case 145 -> Division.FEATHERWEIGHT;
//            case 155 -> Division.LIGHTWEIGHT;
//            case 170 -> Division.WELTERWEIGHT;
//            case 185 -> Division.MIDDLEWEIGHT;
//            case 205 -> Division.LIGHTHEAVYWEIGHT;
//            default -> Division.HEAVYWEIGHT;
//        };
//    }

    public void updateFightRecord(String[] newFightRecord){
        this.fightRecord = FightRecord.toFightRecord(newFightRecord);
    }

    public void updateRanking(int ranking){
        this.ranking = ranking;
    }
}