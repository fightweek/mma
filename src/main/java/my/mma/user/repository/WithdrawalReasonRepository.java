package my.mma.user.repository;

import my.mma.user.entity.WithdrawalReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalReasonRepository extends JpaRepository<WithdrawalReason, Long> {
}
