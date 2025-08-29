package my.mma.global.s3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3JsonFileService {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.chat-bucket}")
    private String bucketName;

    // put 요청으로, 입력받은 key 이름의 파일이 없을 경우 새로 생성, 있을 경우 기존 파일 덮어 씌움
    public void uploadChatLog(String objectKey, String content) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType("application/json")
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromString(content));
    }

}
