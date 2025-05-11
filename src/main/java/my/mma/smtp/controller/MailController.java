package my.mma.smtp.controller;

import lombok.RequiredArgsConstructor;
import my.mma.smtp.dto.VerifyCodeDto;
import my.mma.smtp.service.MailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smtp")
public class MailController {

    private final MailService mailService;

    @PostMapping("/send_join_code")
    public ResponseEntity<String> sendJoinCode(
            @RequestBody Map<String, String> emailTo
    ) {
        mailService.sendJoinCode(emailTo);
        return ResponseEntity.status(HttpStatus.OK).body("sent");
    }

    @PostMapping("/verify_code")
    public ResponseEntity<String> verifyCode(
            @RequestBody VerifyCodeDto verifyCodeDto
    ) {
        System.out.println(verifyCodeDto.getCode());
        if (mailService.verifyCode(verifyCodeDto))
            return ResponseEntity.status(HttpStatus.OK).body("verified");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("verify failed");
    }

}
