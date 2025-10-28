package my.mma.admin.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.event.service.AdminEventService;
import my.mma.admin.event.service.AdminSaveStreamFightEventService;
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
    private final AdminSaveStreamFightEventService saveStreamFightEventService;

    @PostMapping("/save_upcoming")
    public ResponseEntity<Void> saveUpcomingEvents(){
        eventService.saveUpcomingEvents();
        saveStreamFightEventService.saveStreamFightEvent();
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

}
