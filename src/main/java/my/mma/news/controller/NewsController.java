package my.mma.news.controller;

import lombok.extern.slf4j.Slf4j;
import my.mma.news.dto.NewsDto.AdminNewsRequest;
import my.mma.news.dto.NewsDto.NewsTranslationResponse;
import my.mma.news.service.NewsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Objects;

import static my.mma.news.dto.NewsDto.*;
import static my.mma.news.dto.NewsDto.NewsTranslationRequest.*;

@RestController
@Slf4j
@RequestMapping("/news")
public class NewsController {

    @Value("${python.uri}")
    private String pythonURI;

    private final NewsService newsService;
    private final RestTemplate restTemplate;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
        this.restTemplate = new RestTemplate();
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveNews(
            AdminNewsRequest adminNewsRequest
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(pythonURI+"/ufc/news");
        HttpEntity<NewsTranslationRequest> httpEntity = new HttpEntity<>(toDto(adminNewsRequest), headers);
        ResponseEntity<NewsTranslationResponse> translatedResponse = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.POST,
                httpEntity,
                NewsTranslationResponse.class
        );

        newsService.saveNews(Objects.requireNonNull(translatedResponse.getBody()), adminNewsRequest.getMultipartFile());
        return ResponseEntity.status(HttpStatus.CREATED).body("saved");
    }

}
