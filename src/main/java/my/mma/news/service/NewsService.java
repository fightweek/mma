package my.mma.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.news.dto.SaveNewsDto;
import my.mma.news.entity.ImageFile;
import my.mma.news.entity.News;
import my.mma.news.repository.ImageFileRepository;
import my.mma.news.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NewsService {

    @Value("${file.dir}")
    private String fileDir;

    private final NewsRepository newsRepository;
    private final ImageFileRepository imageFileRepository;

    @Transactional
    public void saveNews(SaveNewsDto newsDto){
        MultipartFile multipartFile = newsDto.getMultipartFile();
        String originalFilename = multipartFile.getOriginalFilename();
        int pos = originalFilename.lastIndexOf(".");
        String ext = originalFilename.substring(pos); //확장자 (.png/.jpg...)

        // 서버에 저장하는 파일명 (storeFileName)
        String uuid = UUID.randomUUID().toString();
        String storeFileName = uuid + ext;
        try {
            multipartFile.transferTo(new File(fileDir + storeFileName));
        }catch (IOException e) {
            log.info("file transfer exception, e = ",e);
        }

        ImageFile imageFile = imageFileRepository.save(ImageFile.builder()
                .uploadFileName(originalFilename).storeFileName(storeFileName)
                .build());
        News news = newsDto.toEntity(imageFile);
        newsRepository.save(news);
    }

}
