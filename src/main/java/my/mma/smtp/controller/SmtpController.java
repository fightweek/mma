package my.mma.smtp.controller;

import lombok.RequiredArgsConstructor;
import my.mma.smtp.dto.VerifyCodeRequest;
import my.mma.smtp.service.MailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smtp")
public class SmtpController {

    private final MailService mailService;

    @GetMapping("/check_dup_nickname")
    public ResponseEntity<Boolean> checkDuplicatedNickname(@RequestBody Map<String,String> nickname){
        return ResponseEntity.ok(mailService.checkDuplicatedNickname(nickname.get("nickname")));
    }

    @PostMapping("/send_join_code")
    public ResponseEntity<Boolean> sendJoinCode(
            @RequestBody Map<String, String> emailTo
    ) {
        if(mailService.sendJoinCode(emailTo)){
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.ok().body(false);
    }

    @PostMapping("/verify_code")
    public ResponseEntity<String> verifyCode(
            @RequestBody VerifyCodeRequest verifyCodeRequest
    ) {
        System.out.println(verifyCodeRequest.getCode());
        if (mailService.verifyCode(verifyCodeRequest))
            return ResponseEntity.status(HttpStatus.OK).body("verified");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("verify failed");
    }

}
