package my.mma.global.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.mma.fighter.entity.BaseEntity;
import my.mma.user.entity.User;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Table(uniqueConstraints = {
        @UniqueConstraint(name = "user_target_unique",columnNames = {"user_id","target_type","target_id"})
})
@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Alert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name="alert_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    private Long targetId;

}
