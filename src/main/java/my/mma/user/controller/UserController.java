package my.mma.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my.mma.user.dto.UserDto;
import my.mma.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/check_dup_nickname")
    public ResponseEntity<Boolean> checkDuplicatedNickname(@RequestBody Map<String,String> nickname){
        System.out.println("nickname = " + nickname);
        return ResponseEntity.ok(userService.checkDuplicatedNickname(nickname.get("nickname")));
    }

    @PostMapping("/update_nickname")
    public ResponseEntity<UserDto> updateNickname(HttpServletRequest request, @RequestBody Map<String, String> nickname){
        return ResponseEntity.ok().body(userService.updateNickname(request,nickname.get("nickname")));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(HttpServletRequest request) {
        return ResponseEntity.ok().body(userService.getMe(request));
    }

}
