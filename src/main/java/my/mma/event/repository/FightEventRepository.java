package my.mma.event.repository;

import my.mma.event.entity.FightEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FightEventRepository extends JpaRepository<FightEvent,Long> {
}
