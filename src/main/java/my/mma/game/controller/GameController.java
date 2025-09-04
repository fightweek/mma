package my.mma.game.controller;

import lombok.RequiredArgsConstructor;
import my.mma.game.dto.GameAttemptResponse;
import my.mma.game.dto.GameResponse;
import my.mma.game.service.GameService;
import my.mma.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    @GetMapping("/attempt_count")
    public ResponseEntity<GameAttemptResponse> getGameAttemptCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok().body(gameService.getGameAttemptCount(userDetails.getUsername()));
    }

    @PostMapping("/update_attempt_count")
    public ResponseEntity<Void> subtractAttemptCount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("isSubtract") boolean isSubtract
    ) {
        gameService.updateGameAttemptCount(userDetails.getUsername(), isSubtract);
        return ResponseEntity.ok().body(null);
    }

    @PatchMapping("/update_point")
    public ResponseEntity<Integer> updatePoint(
            @RequestParam("newPoint") String newPoint,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok().body(gameService.updatePoint(userDetails.getUsername(), Integer.parseInt(newPoint)));
    }

    @GetMapping("/start")
    public ResponseEntity<GameResponse> getGameQuestions(@RequestParam("isNormal") boolean isNormal,
                                                         @RequestParam("isImage") boolean isImage) {
        return ResponseEntity.ok().body(gameService.generateGameQuestions(isNormal, isImage));
    }

}
