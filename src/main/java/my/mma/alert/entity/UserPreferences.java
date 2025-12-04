package my.mma.alert.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.mma.alert.constant.AlertTarget;
import my.mma.user.entity.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@Getter
@Builder
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name="user_preferences_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(
            name = "user_preferences_alert_targets",
            joinColumns = @JoinColumn(name = "user_preferences_id")
    )
    @Column(name = "alert_target")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private final Set<AlertTarget> alertTargets = new HashSet<>(Set.of(AlertTarget.values()));

    public void addTarget(AlertTarget alertTarget){
        alertTargets.add(alertTarget);
    }

    public void addAll(){
        alertTargets.addAll(List.of(AlertTarget.values()));
    }

    public void deleteTarget(AlertTarget targetType){
        this.alertTargets.remove(targetType);
    }

    public void clearAllTargets(){
        this.alertTargets.clear();
    }

}
