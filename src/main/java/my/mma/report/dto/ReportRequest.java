package my.mma.stream.dto;

import lombok.*;
import my.mma.stream.entity.Report;
import my.mma.stream.entity.ReportCategory;

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
