package my.mma.bet.controller;

import lombok.RequiredArgsConstructor;
import my.mma.bet.service.BetService;
import my.mma.security.CustomUserDetails;
import my.mma.bet.dto.BetResponse;
import my.mma.bet.dto.SingleBetRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.http.HttpStatusCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bet")
public class BetController {

    private final BetService betService;

    // 기존 user의 betList에 singleBet을 추가하는 작업이므로, PatchMapping
    @PatchMapping("")
    public ResponseEntity<Integer> bet(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody SingleBetRequest betRequest) {
        return ResponseEntity.status(HttpStatusCode.CREATED).body(
                betService.bet(userDetails.getUsername(), betRequest)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<BetResponse> weeklyBetHistory(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @RequestParam(value = "eventId") String eventId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                betService.betHistory(userDetails.getUsername(), Long.parseLong(eventId))
        );
    }

    @DeleteMapping("")
    public ResponseEntity<BetResponse> deleteBet(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestParam(value = "betId") String betId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                betService.deleteBet(userDetails.getUsername(), Long.parseLong(betId))
        );
    }

}
