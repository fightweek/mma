package my.mma.stream.controller;

import lombok.RequiredArgsConstructor;
import my.mma.security.CustomUserDetails;
import my.mma.stream.dto.BlockedUsersDto;
import my.mma.stream.dto.StreamUserDto;
import my.mma.stream.service.BlockedUsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/blocked_users")
@RequiredArgsConstructor
public class BlockedUsersController {

    private final BlockedUsersService blockedUsersService;

    @GetMapping("")
    public ResponseEntity<BlockedUsersDto> getBlockedUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok().body(blockedUsersService.getBlockedUsers(userDetails.getUsername()));
    }

    @PostMapping("")
    public ResponseEntity<Void> releaseBlockedUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "id") Long idToReleaseBlock
    ) {
        blockedUsersService.releaseBlock(userDetails.getUsername(), idToReleaseBlock);
        return ResponseEntity.ok().body(null);
    }

}
