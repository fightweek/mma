package my.mma.bet.repository;

import my.mma.bet.entity.Bet;
import my.mma.user.dto.UserBetRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BetRepository extends JpaRepository<Bet, Long> {

    @Query("select distinct b from Bet b join fetch b.betCards where b.id=:id")
    Optional<Bet> findByIdWithBetCards(Long id);
    @Query("select distinct b from Bet b join fetch b.betCards where b.fightEvent.id=:eventId and b.user.id=:userId")
    List<Bet> findByEventIdAndUserIdWithBetCards(long eventId, long userId);
    @Query("""
            select coalesce(sum(case when b.succeed = true then 1 else 0 end),0) as win,
             coalesce(sum(case when b.succeed = false then 1 else 0 end),0) as loss,
             coalesce(sum(case when b.succeed is null then 1 else 0 end),0) as noContest
             from Bet b where b.user.id=:userId and b.fightEvent.completed = true
            """)
    UserBetRecord getUserBetRecord(@Param("userId") Long userId);
}
