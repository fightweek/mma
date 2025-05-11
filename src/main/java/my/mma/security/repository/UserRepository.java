package my.mma.security.repository;

import my.mma.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameStartingWith(String domain);
    Optional<User> findByEmailAndUsernameIsNull(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailAndUsernameStartingWith(String email, String domain);
}
