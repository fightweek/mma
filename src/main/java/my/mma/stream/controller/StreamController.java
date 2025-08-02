package my.mma.stream.controller;

import lombok.RequiredArgsConstructor;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.security.CustomUserDetails;
import my.mma.stream.dto.bet_and_vote.BetRequest;
import my.mma.stream.dto.bet_and_vote.VoteRateDto;
import my.mma.stream.dto.bet_and_vote.VoteRequest;
import my.mma.stream.service.StreamPredictionService;
import my.mma.stream.service.StreamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.http.HttpStatusCode;

@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;
    private final StreamPredictionService streamPredictionService;

    // stream room 진입 시 최초 1회만 호출 (이후에는 socket을 통한 n분 주기의 broadcast 응답)
    @GetMapping("/today_event")
    public ResponseEntity<StreamFightEventDto> stream(
    ){
        return ResponseEntity.ok().body(streamService.getTodayEvent());
    }

    @PostMapping("/vote")
    public ResponseEntity<VoteRateDto> vote(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody VoteRequest voteRequest){
        return ResponseEntity.status(HttpStatusCode.CREATED).body(
                streamPredictionService.vote(userDetails.getUsername(), voteRequest)
        );
    }

    @PostMapping("/bet")
    public ResponseEntity<Integer> bet(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody BetRequest betRequest){
        return ResponseEntity.status(HttpStatusCode.CREATED).body(
                streamPredictionService.bet(userDetails.getUsername(), betRequest)
        );
    }

}
