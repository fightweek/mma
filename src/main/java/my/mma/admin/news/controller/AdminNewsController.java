package my.mma.admin.news.controller;

import lombok.extern.slf4j.Slf4j;
import my.mma.admin.news.dto.NewsSaveRequest;
import my.mma.admin.news.service.AdminNewsService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/admin/news")
public class AdminNewsController {

    private final AdminNewsService newsService;

    public AdminNewsController(AdminNewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveNews(
            @ModelAttribute NewsSaveRequest request
            ) {
        newsService.saveNews(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
