package my.mma.smtp.repository;

import my.mma.smtp.entity.JoinCode;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JoinCodeRepository extends CrudRepository<JoinCode,Long> {
    Optional<JoinCode> findByEmail(String email);
}
