package my.mma.admin.fighter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.fighter.service.AdminFighterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 기존 이미지 덮어쓰거나 새로 넣는 작업이므로 putmapping
    @PutMapping("/image")
    public ResponseEntity<Void> updateImage(
            @RequestBody Map<String,String> fighterNameMap
    ) {
        fighterService.updateImage(fighterNameMap.get("fighterName"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

}
