package my.mma.admin.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FcmOptions;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fighter.entity.Fighter;
import my.mma.global.entity.TargetType;
import my.mma.global.fcm.FcmMessageService;
import my.mma.global.repository.LikeRepository;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FcmMessageService fcmMessageService;
    private final ObjectMapper objectMapper;

    @Async("notification") // 별도의 스레드풀
    public void sendNotification(String eventName, List<Fighter> fighters){
        System.out.println("========================================");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if(user.getFcmToken() == null)
                continue;
            // event에 포함된 fighters 중 user가 알림 설정한 fighters
            List<String> filteredFighterNames = fighters.stream()
                    .filter(fighter -> likeRepository.existsByUserAndTargetTypeAndTargetId(user, TargetType.FIGHTER, fighter.getId()))
                    .map(Fighter::getName)
                    .toList();
            if(!filteredFighterNames.isEmpty()){
                log.info("send notification to {}, fighters = {}",user.getNickname(),filteredFighterNames);
                StringBuilder body = new StringBuilder(fighters.get(0).getName());
                if(filteredFighterNames.size()>1)
                    body.append(" 외 ").append(filteredFighterNames.size()-1).append("명의 선수");
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
        System.out.println("========================================");
    }

}
