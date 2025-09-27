package my.mma.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.report.dto.ReportRequest;
import my.mma.report.repository.ReportRepository;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    @Transactional
    public void report(String email, ReportRequest request) {
        User user = extractUserByEmail(email);
        reportRepository.save(request.toEntity(user.getId()));
    }

    private User extractUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
    }

}
