package my.mma.bet.repository;

import my.mma.bet.entity.Bet;
import my.mma.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BetRepository extends JpaRepository<Bet, Long> {

    @Query("select distinct b from Bet b join fetch b.betCards")
    Optional<Bet> findByIdWithBetCards(Long id);
}
