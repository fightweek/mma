package my.mma.smtp.controller;

import lombok.RequiredArgsConstructor;
import my.mma.smtp.dto.VerifyCodeRequest;
import my.mma.smtp.service.SmtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smtp")
public class SmtpController {

    private final SmtpService smtpService;

    @PostMapping("/send_join_code")
    public ResponseEntity<Boolean> sendJoinCode(
            @RequestBody Map<String, String> emailTo
    ) {
        if(smtpService.sendJoinCode(emailTo.get("email"))){
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.ok().body(false);
    }

    @PostMapping("/verify_code")
    public ResponseEntity<String> verifyCode(
            @RequestBody VerifyCodeRequest verifyCodeRequest
    ) {
        if (smtpService.verifyCode(verifyCodeRequest))
            return ResponseEntity.status(HttpStatus.OK).body("verified");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("verify failed");
    }

}
