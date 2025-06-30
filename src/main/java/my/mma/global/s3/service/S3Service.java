package my.mma.global.s3.service;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.s3.default-headshot}")
    private String defaultImageUrl;

    public String generateGetObjectPreSignedUrl(String objectKey) {
        objectKey = objectExists(objectKey) ? objectKey : defaultImageUrl;
        GetObjectRequest aclRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder -> builder
                .getObjectRequest(aclRequest)
                .signatureDuration(Duration.ofMinutes(10)));

        return presignedRequest.url().toString();
    }

    private boolean objectExists(String objectKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
            return true;
        } catch (S3Exception e) {
            log.error("e=", e);
            log.error("no such key exception occurred");
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> uploadFile(List<MultipartFile> multipartFiles) {
        List<String> storeFileNames = new ArrayList<>();
        multipartFiles.forEach(file -> {
            String randomFileName = generateRandomFileName(file);
            storeFileNames.add(randomFileName);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key("news/" + randomFileName)
                        .contentType(objectMetadata.getContentType())
                        .build();
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            } catch (IOException e) {
                throw new CustomException(CustomErrorCode.SERVER_ERROR);
            }
        });
        return storeFileNames;
    }

    private String generateRandomFileName(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        int pos = originalFilename.lastIndexOf(".");
        String ext = originalFilename.substring(pos); //확장자 (.png/.jpg...)

        // 서버에 저장하는 파일명 (storeFileName)
        String uuid = UUID.randomUUID().toString();
        return uuid + ext;
    }

}