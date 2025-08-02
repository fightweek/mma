package my.mma.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.FightEventDto;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.event.service.EventService;
import my.mma.global.dto.UpdatePreferenceDto;
import my.mma.global.s3.service.S3Service;
import my.mma.global.service.UpdatePreferenceService;
import my.mma.security.CustomUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;
    private final UpdatePreferenceService updatePreferenceService;

    @GetMapping("/schedule")
    public ResponseEntity<FightEventDto> getSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "date",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        System.out.println("date = " + date);
        return ResponseEntity.ok().body(eventService.getSchedule(date,userDetails.getUsername()));
    }

    @PostMapping("/preference")
    public ResponseEntity<Void> updatePreference(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdatePreferenceDto request
    ) {
        System.out.println("request = " + request);
        updatePreferenceService.updatePreference(userDetails.getUsername(),request);
        return ResponseEntity.ok().body(null);
    }

}
