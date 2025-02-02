package my.mma.fighter.repository;

import my.mma.fighter.entity.FightEvent;
import my.mma.fighter.entity.Fighter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface FightEventRepository extends JpaRepository<FightEvent,Long> {
}
