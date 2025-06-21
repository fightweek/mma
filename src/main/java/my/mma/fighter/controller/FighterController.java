package my.mma.fighter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fighter.dto.FighterDetailDto;
import my.mma.fighter.dto.FighterDto;
import my.mma.fighter.service.FighterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@Slf4j
@RequestMapping("/fighter")
@RequiredArgsConstructor
public class FighterController {

    private final FighterService fighterService;

    @PostMapping("/update_ranking")
    public ResponseEntity<String> updateRanking(){
        fighterService.updateRanking();
        return ResponseEntity.status(HttpStatus.CREATED).body("created");
    }

    @GetMapping("/detail")
    public ResponseEntity<FighterDetailDto> detail(@RequestParam("fighterId") Long fighterId){
        return ResponseEntity.ok().body(fighterService.detail(fighterId));
    }

    @GetMapping("")
    public ResponseEntity<Page<FighterDto>> search(
            @PageableDefault(sort = "id",direction = DESC) Pageable pageable
    ){
        return null;
    }

}