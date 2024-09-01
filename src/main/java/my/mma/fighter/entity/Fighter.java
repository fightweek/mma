package my.mma.fighter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.File;
import java.time.LocalDateTime;

@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Fighter {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fighter_id")
    private Long id;

    private String name;

    private String nickname;

    private String koreanName;

    @Embedded
    private FightRecord fightRecord;

    private File picture;

//    @OneToMany(mappedBy = "fighter")
//    private List<FightHighlight> highlights = new ArrayList<>();

    private Integer ranking;

    private Integer age;

    private Boolean gender;

    @Enumerated(EnumType.STRING)
    private Division division;

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;

}