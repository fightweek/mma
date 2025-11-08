package my.mma.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.mma.security.CustomUserDetails;
import my.mma.user.dto.JoinRequest;
import my.mma.user.dto.UserDto;
import my.mma.user.dto.UserProfileDto;
import my.mma.user.service.UserProfileService;
import my.mma.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @GetMapping("/check_dup_nickname")
    public ResponseEntity<Boolean> checkDuplicatedNickname(@RequestBody Map<String, String> nickname) {
        System.out.println("nickname = " + nickname);
        return ResponseEntity.ok(userService.checkDuplicatedNickname(nickname.get("nickname")));
    }

    @PostMapping("/update_nickname")
    public ResponseEntity<UserDto> updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Map<String, String> nickname) {
        return ResponseEntity.ok().body(userService.updateNickname(userDetails.getUsername(), nickname.get("nickname")));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().body(userService.getMe(userDetails.getUsername()));
    }

    @PostMapping("/join")
    public ResponseEntity<Void> join(@RequestBody JoinRequest request) {
        userService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> profile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return ResponseEntity.ok().body(userProfileService.profile(customUserDetails.getUsername()));
    }

}
