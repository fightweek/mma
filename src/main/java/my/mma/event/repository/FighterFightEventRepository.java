package my.mma.event.repository;

import my.mma.event.entity.FighterFightEvent;
import my.mma.fighter.entity.Fighter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FighterFightEventRepository extends JpaRepository<FighterFightEvent,Long> {
    // 해당 fighter의 모든 fighterFightEvent(previous & upcoming) 정보 불러옴
//    @Query("select ffe from FighterFightEvent ffe where ffe.loser=:fighter or ffe.winner=:fighter order by ffe.fightEvent.eventDate desc")
     @Query("select ffe from FighterFightEvent ffe join fetch ffe.fightEvent join fetch ffe.winner join fetch ffe.loser where ffe.loser=:fighter or ffe.winner=:fighter order by ffe.fightEvent.eventDate desc")
    List<FighterFightEvent> findByFighter(@Param("fighter") Fighter fighter);
}
