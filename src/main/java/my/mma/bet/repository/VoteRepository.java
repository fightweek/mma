package my.mma.bet.repository;

import my.mma.bet.entity.Vote;
import my.mma.event.entity.FighterFightEvent;
import my.mma.stream.dto.bet_and_vote.VoteCntDto;
import my.mma.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Query("""
            select sum(case when v.winnerId = :winnerId then 1 else 0 end) as winnerVotes,
             sum(case when v.winnerId = :loserId then 1 else 0 end) as loserVotes
             from Vote v where v.fighterFightEvent.id=:ffeId
            """)
    VoteCntDto countVoteByFfeIdAndFighters(
            @Param("ffeId") Long ffeId, @Param("winnerId") Long winnerId, @Param("loserId") Long loserId
    );
    Optional<Vote> findByUserAndFighterFightEvent(User user, FighterFightEvent ffe);
}