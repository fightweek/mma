package my.mma.report.controller;

import lombok.RequiredArgsConstructor;
import my.mma.report.dto.ReportRequest;
import my.mma.report.service.ReportService;
import my.mma.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("")
    public ResponseEntity<Void> report(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody ReportRequest request) {
        reportService.report(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

}
