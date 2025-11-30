package my.mma.user.repository;

import my.mma.user.entity.WithdrawnUserEmail;
import org.springframework.data.repository.CrudRepository;

public interface WithdrawnEmailRepository extends CrudRepository<WithdrawnUserEmail, String> {
}
