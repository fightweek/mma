package my.mma.alert.service;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.alert.constant.AlertTarget;
import my.mma.alert.repository.UserPreferencesRepository;
import my.mma.fightevent.dto.StreamFightEventDto;
import my.mma.global.fcm.FcmMessageService;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyFightEventNotificationService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final FcmMessageService fcmMessageService;
    private final RedisUtils<StreamFightEventDto> weeklyFightEventRedisUtils;

    @Scheduled(cron = "0 0 19 ? * WED")
    public void sendWeeklyFightEventAlert() {
        StreamFightEventDto weeklyFightEvent = weeklyFightEventRedisUtils.getData("current-event");
        if (weeklyFightEvent == null) {
            log.warn("No weekly fight event found in Redis");
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate eventDate = weeklyFightEvent.getDate();
        long diff = ChronoUnit.DAYS.between(today, eventDate);
        if (diff < 0 || diff > 7) {
            return; // 과거거나 7일 초과면 전송 X
        }
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getFcmToken() == null) continue;

            userPreferencesRepository.findByUserId(user.getId()).ifPresent(userPref -> {
                if (userPref.getAlertTargets().contains(AlertTarget.WEEKLY_EVENT)) {
                    log.info("send notification to {}", user.getNickname());
                    Message message = Message.builder()
                            .setToken(user.getFcmToken())
                            .setNotification(Notification.builder()
                                    .setTitle("이번 주 경기 안내")
                                    .setBody("%s 경기가 %s에서 열립니다. 지금 카드를 확인해보세요!"
                                            .formatted(
                                                    weeklyFightEvent.getName(),
                                                    weeklyFightEvent.getLocation()
                                            ))
                                    .build())
                            .build();
                    fcmMessageService.sendMessage(message);
                }
            });
        }
    }
}

