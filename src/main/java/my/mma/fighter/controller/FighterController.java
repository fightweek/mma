package my.mma.fighter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fighter.dto.FighterDetailDto;
import my.mma.fighter.dto.FighterDto;
import my.mma.fighter.service.FighterService;
import my.mma.global.dto.UpdatePreferenceRequest;
import my.mma.global.entity.TargetType;
import my.mma.global.s3.service.S3ImgService;
import my.mma.global.service.UpdatePreferenceService;
import my.mma.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
    private final S3ImgService s3Service;
    private final UpdatePreferenceService updatePreferenceService;

    @GetMapping("/{fighterId}")
    public ResponseEntity<FighterDetailDto> detail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("fighterId") Long fighterId) {
        return ResponseEntity.ok().body(fighterService.detail(userDetails.getUsername(), fighterId));
    }

    @GetMapping("/fighters")
    public ResponseEntity<Page<FighterDto>> search(
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(sort = "name", direction = ASC) Pageable pageable
    ) {
        return ResponseEntity.ok().body(fighterService.search(name, pageable));
    }

    @PostMapping("/preference")
    public ResponseEntity<Void> updatePreference(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Validated UpdatePreferenceRequest request
    ) {
        updatePreferenceService.updatePreference(userDetails.getUsername(), request, TargetType.FIGHTER);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/headshot")
    public ResponseEntity<Map<String, String>> headshotUrl(
            @RequestParam("name") String name
    ) {
        String preSignedUrl = s3Service.generateImgUrl(
                "headshot/" + name.replace(' ', '-') + ".png", 1);
        Map<String, String> map = new HashMap<>();
        map.put("url", preSignedUrl);
        return ResponseEntity.ok().body(map);
    }

    @GetMapping("/body")
    public ResponseEntity<Map<String, String>> bodyUrl(
            @RequestParam("name") String name
    ) {
        String preSignedUrl = s3Service.generateImgUrl(
                "body/" + name.replace(' ', '-') + ".png", 1);
        Map<String, String> map = new HashMap<>();
        map.put("url", preSignedUrl);
        return ResponseEntity.ok().body(map);
    }

}