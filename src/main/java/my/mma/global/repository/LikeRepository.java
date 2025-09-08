package my.mma.global.repository;

import my.mma.global.entity.Like;
import my.mma.global.entity.TargetType;
import my.mma.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserAndTargetTypeAndTargetId(User user, TargetType targetType, Long targetId);
    void deleteByUserAndTargetTypeAndTargetId(User user, TargetType targetType, Long targetId);
}
