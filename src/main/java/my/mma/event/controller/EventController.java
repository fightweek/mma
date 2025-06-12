package my.mma.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.event.dto.FightEventDto;
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
    private final S3Service s3Service;

    @GetMapping("/schedule")
    public ResponseEntity<FightEventDto> getSchedule(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        System.out.println("date = " + date);
        return ResponseEntity.ok().body(eventService.getSchedule(date));
    }

    @GetMapping("/presignedUrl")
    public ResponseEntity<Map<String,String>> getFighterPresignedUrl(
            @RequestParam("name") String name
    ){
        String preSignedUrl = s3Service.generateGetObjectPreSignedUrl(
                "headshot/" + name.replace(' ', '-') + ".png");
        Map<String, String> map = new HashMap<>();
        map.put("url",preSignedUrl);
        return ResponseEntity.ok().body(map);
    }

}
