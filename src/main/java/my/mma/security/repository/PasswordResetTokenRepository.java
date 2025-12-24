package my.mma.security.repository;

import my.mma.security.entity.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {
}
