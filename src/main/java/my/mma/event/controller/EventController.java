package my.mma.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.FightEventDto;
import my.mma.event.service.EventService;
import my.mma.global.dto.UpdatePreferenceRequest;
import my.mma.global.service.UpdatePreferenceService;
import my.mma.security.CustomUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;
    private final UpdatePreferenceService updatePreferenceService;
    private Integer visitedCount = 0;

    @GetMapping("/schedule")
    public ResponseEntity<FightEventDto> getSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "date",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        System.out.println("visitedCount = " + ++visitedCount);
        return ResponseEntity.ok().body(eventService.getSchedule(date,userDetails.getUsername()));
    }

    @PostMapping("/preference")
    public ResponseEntity<Void> updatePreference(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdatePreferenceRequest request
    ) {
        System.out.println("request = " + request);
        updatePreferenceService.updatePreference(userDetails.getUsername(),request);
        return ResponseEntity.ok().body(null);
    }

}
