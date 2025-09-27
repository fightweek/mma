package my.mma.bet.repository;

import my.mma.bet.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BetRepository extends JpaRepository<Bet, Long> {

    @Query("select distinct b from Bet b join fetch b.betCards where b.id=:id")
    Optional<Bet> findByIdWithBetCards(Long id);
    @Query("select distinct b from Bet b join fetch b.betCards where b.eventId=:eventId and b.user.id=:userId")
    List<Bet> findByEventIdAndUserIdWithBetCards(long eventId, long userId);
}
