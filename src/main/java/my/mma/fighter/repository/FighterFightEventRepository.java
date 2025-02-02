package my.mma.fighter.repository;

import my.mma.fighter.entity.FighterFightEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FighterFightEventRepository extends JpaRepository<FighterFightEvent,Long> {
}
