package my.mma.admin.stream.event.controller;

import lombok.RequiredArgsConstructor;
import my.mma.admin.stream.event.service.AdminStreamEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/stream/event")
@RequiredArgsConstructor
public class AdminStreamEventController {

    private final AdminStreamEventService adminStreamEventService;

    @PostMapping("")
    public ResponseEntity<Boolean> startPolling() {
        return ResponseEntity.ok().body(adminStreamEventService.startPolling());
    }

}
