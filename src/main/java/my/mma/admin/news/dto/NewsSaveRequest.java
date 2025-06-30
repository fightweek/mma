package my.mma.admin.news.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.mma.news.entity.News;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsSaveRequest {

    private String title;

    private String content;

    private String source;

    private List<MultipartFile> multipartFiles;

    public News toEntity() {
        return News.builder()
                .title(title)
                .content(content)
                .likes(0)
                .source(source)
                .imageFiles(new ArrayList<>())
                .build();
    }
}

//    @Getter
//    @Setter
//    @AllArgsConstructor
//    @NoArgsConstructor(access = AccessLevel.PROTECTED)
//    @Builder
//    public static class NewsTranslationRequest {
//
//        private String title;
//
//        private List<String> storeFileNames;
//
//        private List<String> fileBytes;
//
//        public static NewsTranslationRequest toDto(AdminNewsRequest request, List<String> storeFileNames) {
//            return NewsTranslationRequest.builder()
//                    .title(request.getTitle())
//                    .storeFileNames(storeFileNames)
//                    // Base64를 통해 2차 인코딩 (byte 배열 -- 2차 인코딩 --> json 형식으로 전송할 수 있는 인코딩된 텍스트)
//                    .fileBytes(request.getMultipartFiles().stream().map(
//                            NewsDto::encodeToString
//                    ).collect(Collectors.toList()))
//                    .build();
//        }
//    }
//
//    public static String encodeToString(MultipartFile multipartFile) {
//        try {
//            return Base64.getEncoder().encodeToString(multipartFile.getBytes());
//        } catch (IOException e) {
//            throw new CustomException(CustomErrorCode.SERVER_ERROR);
//        }
//    }
//
//    @Getter
//    @Setter
//    @AllArgsConstructor
//    @NoArgsConstructor(access = AccessLevel.PROTECTED)
//    @Builder
//    public static class NewsTranslationResponse {
//
//        private String title;
//
//        private String content;
//
//        private String source;
//
//        public News toEntity() {
//            return News.builder()
//                    .likes(0)
//                    .source(null)
//                    .content(content)
//                    .title(title)
//                    .source(source)
//                    .imageFiles(new ArrayList<>())
//                    .build();
//        }
//    }
