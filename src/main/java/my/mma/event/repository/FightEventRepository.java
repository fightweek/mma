package my.mma.event.repository;

import my.mma.event.entity.FightEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FightEventRepository extends JpaRepository<FightEvent,Long> {
    List<FightEvent> findAllByCompleted(boolean completed);
    Optional<FightEvent> findByEventName(String eventName);
}
