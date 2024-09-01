package my.mma.security.repository;

import my.mma.security.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    boolean existsByLoginId(String loginId);
    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findBySocialId(String socialId);
    Optional<UserEntity> findByUsername(String username);
}
