package my.mma.fightevent.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fightevent.dto.FightEventDto;
import my.mma.fightevent.dto.FighterFightEventCardDetailDto;
import my.mma.fightevent.service.FightEventService;
import my.mma.global.dto.UpdatePreferenceRequest;
import my.mma.global.entity.TargetType;
import my.mma.global.service.UpdatePreferenceService;
import my.mma.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/event")
public class FightEventController {

    private final FightEventService eventService;
    private final UpdatePreferenceService updatePreferenceService;

    // get eventDate for parameter
    @GetMapping("/detail")
    public ResponseEntity<FightEventDto> detail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "date",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok().body(eventService.getSchedule(date,userDetails.getUsername()));
    }

    @GetMapping("/events")
    public ResponseEntity<Page<FightEventDto.FighterFightEventDto>> search(
            @RequestParam(value = "name",defaultValue = "") String name,
            @PageableDefault(sort = "eventDate", direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok().body(eventService.search(name,pageable));
    }

    @GetMapping("/card/detail")
    public ResponseEntity<FighterFightEventCardDetailDto> cardDetail(
            @RequestParam(value = "cardId") String cardId
    ){
        return ResponseEntity.ok().body(eventService.cardDetail(Long.parseLong(cardId)));
    }

    @PostMapping("/preference")
    public ResponseEntity<Void> updatePreference(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdatePreferenceRequest request
    ) {
        updatePreferenceService.updatePreference(userDetails.getUsername(),request, TargetType.EVENT);
        return ResponseEntity.ok().body(null);
    }

}
