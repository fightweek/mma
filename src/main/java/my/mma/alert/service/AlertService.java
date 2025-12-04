package my.mma.alert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.alert.dto.UpdatePreferenceRequest;
import my.mma.alert.dto.UserPreferencesDto;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.alert.dto.UpdateAlertRequest;
import my.mma.alert.entity.Alert;
import my.mma.alert.repository.AlertRepository;
import my.mma.user.entity.User;
import my.mma.alert.entity.UserPreferences;
import my.mma.alert.repository.UserPreferencesRepository;
import my.mma.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final UserPreferencesRepository userPreferencesRepository;

    @Transactional
    public void updateSingleAlert(String email, UpdateAlertRequest request) {
        User user = getUser(email);
        if (request.on()) {
            userPreferencesRepository.findByUserId(user.getId()).ifPresent(
                    userPref -> userPref.addTarget(request.alertTarget())
            );
            Alert alert = request.toEntity(user);
            alertRepository.save(alert);
        } else {
            alertRepository.deleteByUserAndAlertTargetAndTargetId(user, request.alertTarget(), request.targetId());
        }
    }

    public UserPreferencesDto getUserPreferences(String email) {
        User user = getUser(email);
        UserPreferences userPreferences = extractUserPreference(user.getId());
        return new UserPreferencesDto(userPreferences.getAlertTargets());
    }

    // 설정 화면에서 모든 alertTarget on/off
    @Transactional
    public void updateAllPreferences(String email, boolean isOn) {
        User user = getUser(email);
        UserPreferences userPreferences = extractUserPreference(user.getId());
        if (isOn) {
            userPreferences.addAll();
        } else {
            alertRepository.deleteAllByUserId(user.getId());
            userPreferences.clearAllTargets();
        }
    }

    // 설정 화면에서 특정 alertTarget off
    @Transactional
    public void updateSinglePreference(String email, UpdatePreferenceRequest request) {
        User user = getUser(email);
        UserPreferences userPreferences = extractUserPreference(user.getId());
        if (request.on())
            userPreferences.addTarget(request.alertTarget());
        else {
            userPreferences.deleteTarget(request.alertTarget());
            alertRepository.deleteAllByUserIdAndAlertTarget(user.getId(), request.alertTarget());
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
    }

    public UserPreferences extractUserPreference(Long userId) {
        return userPreferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400));
    }

}
