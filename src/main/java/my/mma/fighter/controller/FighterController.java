package my.mma.fighter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fighter.dto.FighterDetailDto;
import my.mma.fighter.dto.FighterDto;
import my.mma.fighter.service.FighterService;
import my.mma.global.dto.UpdatePreferenceDto;
import my.mma.global.s3.service.S3Service;
import my.mma.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.ASC;

@RestController
@Slf4j
@RequestMapping("/fighter")
@RequiredArgsConstructor
public class FighterController {

    private final FighterService fighterService;
    private final S3Service s3Service;

    @GetMapping("/detail")
    public ResponseEntity<FighterDetailDto> detail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("fighterId") Long fighterId) {
        return ResponseEntity.ok().body(fighterService.detail(userDetails.getUsername(), fighterId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<FighterDto>> search(
            @RequestParam(value = "name",defaultValue = "") String name,
            @PageableDefault(sort = "name", direction = ASC) Pageable pageable
    ) {
        return ResponseEntity.ok().body(fighterService.search(name,pageable));
    }

    @PostMapping("/update_preference")
    public ResponseEntity<Void> updatePreference(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdatePreferenceDto request
    ) {
        System.out.println("request = " + request);
        fighterService.updatePreference(userDetails.getUsername(),request);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/headshot")
    public ResponseEntity<Map<String,String>> headshotUrl(
            @RequestParam("name") String name
    ){
        String preSignedUrl = s3Service.generateGetObjectPreSignedUrl(
                "headshot/" + name.replace(' ', '-') + ".png");
        Map<String, String> map = new HashMap<>();
        map.put("url",preSignedUrl);
        return ResponseEntity.ok().body(map);
    }

    @GetMapping("/body")
    public ResponseEntity<Map<String,String>> bodyUrl(
            @RequestParam("name") String name
    ){
        String preSignedUrl = s3Service.generateGetObjectPreSignedUrl(
                "body/" + name.replace(' ', '-') + ".png");
        Map<String, String> map = new HashMap<>();
        map.put("url",preSignedUrl);
        return ResponseEntity.ok().body(map);
    }

}