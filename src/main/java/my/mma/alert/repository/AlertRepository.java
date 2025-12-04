package my.mma.alert.repository;

import my.mma.alert.entity.Alert;
import my.mma.alert.constant.AlertTarget;
import my.mma.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    boolean existsByUserAndAlertTargetAndTargetId(User user, AlertTarget targetType, Long targetId);

    void deleteByUserAndAlertTargetAndTargetId(User user, AlertTarget targetType, Long targetId);

    List<Alert> findByUserId(Long userId);

    void deleteAllByUserId(Long userId);

    void deleteAllByUserIdAndAlertTarget(Long userId, AlertTarget alertTarget);
}
