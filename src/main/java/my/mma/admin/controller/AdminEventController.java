package my.mma.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.service.AdminEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/event")
public class AdminEventController {

    private final AdminEventService eventService;

    // 이 함수는 1~2주 간격으로 호출
    @PostMapping("/upcoming")
    public ResponseEntity<?> saveUpcomingEvent(){
        eventService.saveUpcomingEvent();
        return ResponseEntity.status(HttpStatus.CREATED).body("saved");
    }

}
