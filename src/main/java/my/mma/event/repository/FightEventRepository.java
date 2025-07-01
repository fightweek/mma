package my.mma.event.repository;

import my.mma.event.entity.FightEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FightEventRepository extends JpaRepository<FightEvent, Long> {
    @Query("select distinct f from FightEvent f join fetch f.fighterFightEvents ffe " +
            "join fetch ffe.loser join fetch ffe.winner where f.completed=:completed")
    List<FightEvent> findAllByCompletedWithFighterFightEvents(@Param("completed") boolean completed);

    @EntityGraph(attributePaths = {
            "fighterFightEvents",
            "fighterFightEvents.winner",
            "fighterFightEvents.loser"
    })
    Optional<FightEvent> findByEventDate(LocalDate date);

    Optional<FightEvent> findByName(String eventName);
}
