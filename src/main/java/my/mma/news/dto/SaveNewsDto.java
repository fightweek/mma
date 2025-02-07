package my.mma.news.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import my.mma.news.entity.ImageFile;
import my.mma.news.entity.News;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Slf4j
public class SaveNewsDto {

    private String title;

    private String content;

    private MultipartFile multipartFile;

    public News toEntity(ImageFile imageFile){
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
