package my.mma.game.controller;

import lombok.RequiredArgsConstructor;
import my.mma.game.dto.GameQuestionsDto;
import my.mma.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    @GetMapping("/start")
    public ResponseEntity<GameQuestionsDto> getGameQuestions(@RequestParam("normal") boolean isNormal){
        return ResponseEntity.ok().body(gameService.generateGameQuestions(isNormal));
    }

}
