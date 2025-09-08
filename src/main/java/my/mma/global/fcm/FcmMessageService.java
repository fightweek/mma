package my.mma.global.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
public class FcmMessageService {

    public void sendMessage(Message message){
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new CustomException(CustomErrorCode.SERVER_ERROR,"exception while sending message to firebase");
        }
    }

}
