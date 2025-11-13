package my.mma.fightevent.repository;

import my.mma.fightevent.entity.FightEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FightEventRepository extends JpaRepository<FightEvent, Long> {
    @EntityGraph(attributePaths = {
            "fighterFightEvents",
            "fighterFightEvents.winner",
            "fighterFightEvents.loser"
    })
    List<FightEvent> findByCompletedIsFalse();

    @EntityGraph(attributePaths = {
            "fighterFightEvents",
            "fighterFightEvents.winner",
            "fighterFightEvents.loser"
    })
    Optional<FightEvent> findByEventDate(LocalDate date);

    @EntityGraph(attributePaths = {
            "fighterFightEvents",
            "fighterFightEvents.winner",
            "fighterFightEvents.loser"
    })
    Optional<FightEvent> findByName(String eventName);

    FightEvent findFirstByCompletedIsFalseOrderByEventDateAsc();

    Optional<Page<FightEvent>> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
