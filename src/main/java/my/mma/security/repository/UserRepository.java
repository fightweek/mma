package my.mma.security.repository;

import my.mma.security.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Member,Long> {
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);
    Optional<Member> findBySocialId(String socialId);
    Optional<Member> findByUsername(String username);
}
