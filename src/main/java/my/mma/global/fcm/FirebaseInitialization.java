package my.mma.global.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class FirebaseInitialization {

    @PostConstruct
    public void initialize(){
        try {
            FileInputStream serviceAccount = new FileInputStream
                    ("/my-files/mma-project-7dfc2-firebase-adminsdk-fbsvc-1be46bb027.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException e) {
            throw new CustomException(CustomErrorCode.SERVER_ERROR,"firebase admin file not found");
        } catch (IOException e) {
            throw new CustomException(CustomErrorCode.SERVER_ERROR,"firebase set Credentials error");
        }
    }

}
