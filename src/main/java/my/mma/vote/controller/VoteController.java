package my.mma.vote.controller;

import lombok.RequiredArgsConstructor;
import my.mma.security.CustomUserDetails;
import my.mma.vote.dto.VoteRateDto;
import my.mma.vote.dto.VoteRequest;
import my.mma.vote.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.http.HttpStatusCode;

@RestController
@RequestMapping("/vote")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("")
    public ResponseEntity<VoteRateDto> vote(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody VoteRequest voteRequest) {
        return ResponseEntity.status(HttpStatusCode.CREATED).body(
                voteService.vote(userDetails.getUsername(), voteRequest)
        );
    }

}
