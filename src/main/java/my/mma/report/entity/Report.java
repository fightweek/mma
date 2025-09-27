package my.mma.report.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    private Long reporterId;

    private Long reportedId;

    private Boolean reviewed;

    @Enumerated(EnumType.STRING)
    private ReportCategory reportCategory;

}
