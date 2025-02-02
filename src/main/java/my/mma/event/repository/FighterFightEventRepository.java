package my.mma.event.repository;

import my.mma.event.entity.FighterFightEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FighterFightEventRepository extends JpaRepository<FighterFightEvent,Long> {
}
