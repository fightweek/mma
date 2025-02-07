package my.mma.news.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.news.dto.SaveNewsDto;
import my.mma.news.service.NewsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    @PostMapping("/save")
    public ResponseEntity<String> saveNews(
            SaveNewsDto newsDto
    ){
        newsService.saveNews(newsDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("saved");
    }

}
