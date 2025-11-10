package my.mma.smtp.controller;

import lombok.RequiredArgsConstructor;
import my.mma.smtp.dto.VerifyCodeRequest;
import my.mma.smtp.service.SmtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smtp")
public class SmtpController {

    private final SmtpService smtpService;

    @PostMapping("")
    public ResponseEntity<Boolean> sendJoinCode(
            @RequestBody Map<String, String> emailTo
    ) {
        return ResponseEntity.ok().body(smtpService.sendJoinCode(emailTo.get("email")));
    }

    @DeleteMapping("")
    public ResponseEntity<Void> verifyCode(
            @RequestBody @Validated VerifyCodeRequest verifyCodeRequest
    ) {
        if (smtpService.verifyCode(verifyCodeRequest))
            return ResponseEntity.status(HttpStatus.OK).body(null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

}
