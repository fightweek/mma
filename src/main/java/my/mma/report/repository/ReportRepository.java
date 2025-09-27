package my.mma.stream.repository;

import my.mma.stream.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report,Long> {
}
