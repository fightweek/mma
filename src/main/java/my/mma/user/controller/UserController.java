package my.mma.user.controller;

import lombok.RequiredArgsConstructor;
import my.mma.security.CustomUserDetails;
import my.mma.user.dto.JoinRequest;
import my.mma.user.dto.WithdrawalReasonDto;
import my.mma.user.dto.UserDto;
import my.mma.user.dto.UserProfileDto;
import my.mma.user.service.UserProfileService;
import my.mma.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @GetMapping("/dup_nickname")
    public ResponseEntity<Boolean> checkDuplicatedNickname(@RequestBody Map<String, String> nicknameMap) {
        return ResponseEntity.ok(userService.checkDuplicatedNickname(nicknameMap.get("nickname")));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().body(userService.getMe(userDetails.getUsername()));
    }

    @PatchMapping("/nickname")
    public ResponseEntity<UserDto> updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Map<String, String> nicknameMap) {
        return ResponseEntity.ok().body(userService.updateNickname(userDetails.getUsername(), nicknameMap.get("nickname")));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Map<String, String> passwordMap) {
        userService.updatePassword(userDetails.getUsername(), passwordMap.get("password"));
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("")
    public ResponseEntity<Void> join(@RequestBody @Validated JoinRequest request) {
        userService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @DeleteMapping("")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody WithdrawalReasonDto withdrawalDto) {
        userService.delete(userDetails.getUsername(), withdrawalDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> profile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return ResponseEntity.ok().body(userProfileService.profile(customUserDetails.getUsername()));
    }

}
