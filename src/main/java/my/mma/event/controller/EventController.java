package my.mma.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.FightEventDto;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.event.service.EventService;
import my.mma.global.s3.service.S3Service;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/schedule")
    public ResponseEntity<FightEventDto> getSchedule(
            @RequestParam(name = "date",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "name",required = false) String name
    ) {
        System.out.println("date = " + date);
        return ResponseEntity.ok().body(eventService.getSchedule(date,name));
    }

    // stream room 진입 시 최초 1회만 호출 (이후에는 socket을 통한 n분 주기의 broadcast 응답)
    @GetMapping("/stream")
    public ResponseEntity<StreamFightEventDto> stream(
    ){
        return ResponseEntity.ok().body(eventService.stream());
    }

}
