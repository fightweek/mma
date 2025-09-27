package my.mma.report.dto;

import lombok.*;
import my.mma.report.entity.Report;
import my.mma.report.entity.ReportCategory;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReportRequest {

    private Long reportedId;
    private ReportCategory reportCategory;

    public Report toEntity(Long reporterId){
        return Report.builder()
                .reporterId(reporterId)
                .reportedId(reportedId)
                .reportCategory(reportCategory)
                .build();
    }

}
