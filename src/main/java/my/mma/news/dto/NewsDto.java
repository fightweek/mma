package my.mma.news.dto;

import lombok.*;
import my.mma.news.entity.ImageFile;
import my.mma.news.entity.News;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;


public class NewsDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class AdminNewsRequest {

        private String title;

        private MultipartFile multipartFile;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class NewsTranslationRequest {

        private String title;

        private String fileBytes;

        public static NewsTranslationRequest toDto(AdminNewsRequest request) {
            try {
                return NewsTranslationRequest.builder()
                        .title(request.getTitle())
                        // Base64를 통해 2차 인코딩 (byte 배열 -- 2차 인코딩 --> json 형식으로 전송할 수 있는 인코딩된 텍스트)
                        .fileBytes(Base64.getEncoder().encodeToString(request.getMultipartFile().getBytes()))
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class NewsTranslationResponse {

        private String title;

        private String content;

        public News toEntity(ImageFile imageFile, MultipartFile multipartFile){
            if(multipartFile == null){
                return News.builder()
                        .likes(0)
                        .source(null)
                        .content(content)
                        .title(title)
                        .imageFile(null)
                        .build();
            }else {
                return News.builder()
                        .likes(0)
                        .source(null)
                        .content(content)
                        .title(title)
                        .imageFile(imageFile)
                        .build();
            }
        }
    }
}
