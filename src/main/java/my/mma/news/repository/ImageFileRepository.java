package my.mma.news.repository;

import my.mma.news.entity.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageFileRepository extends JpaRepository<ImageFile, Long> {
}
