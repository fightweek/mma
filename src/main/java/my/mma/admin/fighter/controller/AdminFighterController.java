package my.mma.admin.fighter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.fighter.service.AdminFighterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/admin/fighter")
@RequiredArgsConstructor
public class AdminFighterController {

    private final AdminFighterService fighterService;

    @PostMapping("/update_ranking")
    public ResponseEntity<Void> updateRanking() {
        fighterService.updateRanking();
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PostMapping("/save_game_fighters")
    public ResponseEntity<Void> saveAdminChosenFightersForGame(
            @RequestBody List<String> chosenFighters
    ) {
        fighterService.saveAdminChosenFighters(chosenFighters);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

}
