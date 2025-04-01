package my.mma.security.repository;

import my.mma.security.entity.Refresh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshRepository extends CrudRepository<Refresh,Long> {
    Boolean existsByToken(String token);
    void deleteByToken(String token);
    Optional<Refresh> findByToken(String token);
}
