package my.mma.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.dto.UpdatePreferenceRequest;
import my.mma.global.entity.Alert;
import my.mma.global.entity.TargetType;
import my.mma.global.repository.AlertRepository;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class UpdatePreferenceService {

    private final UserRepository userRepository;
    private final AlertRepository alertRepository;

    @Transactional
    public void updatePreference(String email, UpdatePreferenceRequest request, TargetType targetType) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
        if (request.isOn()) {
            Alert alert = Alert.builder()
                    .user(user)
                    .targetType(targetType)
                    .targetId(request.getTargetId())
                    .build();
            alertRepository.save(alert);
        } else {
            alertRepository.deleteByUserAndTargetTypeAndTargetId(user, TargetType.EVENT, request.getTargetId());
        }
    }

}
