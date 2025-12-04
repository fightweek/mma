package my.mma.alert.service;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.alert.repository.AlertRepository;
import my.mma.alert.repository.UserPreferencesRepository;
import my.mma.fighter.entity.Fighter;
import my.mma.global.fcm.FcmMessageService;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

import static my.mma.alert.constant.AlertTarget.FIGHTER;

/**
 * User가 푸시 알림 등록한 fighter에 대하여 fcm 기반 전역 알림 전송
 * 이번 주 FightEvent DB에 저장되기 직전에 호출됨. (Notification -> Save FightEvent -> Save StreamFightEvent)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FighterNotificationService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final AlertRepository alertRepository;
    private final FcmMessageService fcmMessageService;

    @Async("fighter notification") // 별도의 스레드풀
    public void sendNotification(String eventName, List<Fighter> fighters) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getFcmToken() == null)
                continue;
            userPreferencesRepository.findByUserId(user.getId()).ifPresent(userPref -> {
                if (userPref.getAlertTargets().contains(FIGHTER)) {
                    // event에 포함된 fighters 중 user가 알림 설정한 fighters 로 filtering
                    List<String> filteredFighterNames = fighters.stream()
                            .filter(fighter -> alertRepository.existsByUserAndAlertTargetAndTargetId(user, FIGHTER, fighter.getId()))
                            .map(Fighter::getName)
                            .toList();
                    if (!filteredFighterNames.isEmpty()) {
                        log.info("send notification to {}, fighters = {}", user.getNickname(), filteredFighterNames);
                        StringBuilder body = new StringBuilder(fighters.get(0).getName());
                        if (filteredFighterNames.size() > 1)
                            body.append(" 외 ").append(filteredFighterNames.size() - 1).append("명의 선수");
                        body.append("의 경기가 ").append(eventName).append("에 잡혔습니다!");

                        Message message = Message.builder()
                                .setToken(user.getFcmToken())
                                .setNotification(Notification.builder()
                                        .setTitle("경기 알림")
                                        .setBody(body.toString())
                                        .build())
                                .build();
                        fcmMessageService.sendMessage(message);
                        log.info("Successfully sent message: {}", message);
                    }
                }
            });
        }
    }
}
