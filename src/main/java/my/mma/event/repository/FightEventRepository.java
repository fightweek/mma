package my.mma.event.repository;

import my.mma.event.entity.FightEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FightEventRepository extends JpaRepository<FightEvent, Long> {
    List<FightEvent> findAllByCompleted(boolean completed);

    @EntityGraph(attributePaths = {
            "fighterFightEvents",
            "fighterFightEvents.winner",
            "fighterFightEvents.loser"
    })
    Optional<FightEvent> findByEventDate(LocalDate date);

    Optional<FightEvent> findByName(String eventName);
}
