package my.mma.fighter.repository;

import my.mma.fighter.entity.Fighter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FighterRepository extends JpaRepository<Fighter, Long> {
    Optional<Fighter> findByName(String name);

    Optional<Page<Fighter>> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<List<Fighter>> findFightersByRankingIsNotNull();

    Optional<Fighter> findByNameAndNicknameIsNotNull(String name);
    Optional<Fighter> findByNameAndFightRecordIsNotNull(String name);

    @Query("select f.name from Fighter f")
    List<String> findEveryNames();

}
