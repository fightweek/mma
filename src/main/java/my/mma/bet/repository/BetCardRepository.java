package my.mma.bet.repository;

import my.mma.bet.entity.BetCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetCardRepository extends JpaRepository<BetCard,Long> {
}
