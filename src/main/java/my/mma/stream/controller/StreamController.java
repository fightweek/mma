package my.mma.stream.controller;

import lombok.RequiredArgsConstructor;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.stream.service.StreamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    // stream room 진입 시 최초 1회만 호출 (이후에는 socket을 통한 n분 주기의 broadcast 응답)
    @GetMapping("/weekly_event")
    public ResponseEntity<StreamFightEventDto> weeklyEvent(
    ) {
        return ResponseEntity.ok().body(streamService.getWeeklyEvent());
    }

}
