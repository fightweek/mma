package my.mma.admin.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.news.dto.NewsSaveRequest;
import my.mma.global.s3.service.S3Service;
import my.mma.news.entity.ImageFile;
import my.mma.news.entity.News;
import my.mma.news.repository.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminNewsService {

    private final NewsRepository newsRepository;
    private final S3Service s3Service;

    @Transactional
    public void saveNews(NewsSaveRequest request) {
        System.out.println("request.getMultipartFiles() = " + request.getMultipartFiles());
        List<ImageFile> imageFiles = new ArrayList<>();
        News news = request.toEntity();
        if(request.getMultipartFiles()!=null) {
            List<String> storeFileNames = s3Service.uploadFile(request.getMultipartFiles());
            for (String storeFileName : storeFileNames) {
                ImageFile imageFile = ImageFile.builder()
                        .storeFileName(storeFileName)
                        .build();
                imageFiles.add(imageFile);
            }
            for (ImageFile imageFile : imageFiles) {
                imageFile.addNews(news);
            }
        }
        newsRepository.save(news);
    }

}
