package my.mma.smtp.controller;

import lombok.RequiredArgsConstructor;
import my.mma.smtp.dto.EmailVerificationSendResult;
import my.mma.smtp.dto.PasswordResetTokenResponse;
import my.mma.smtp.dto.EmailVerificationCodeRequest;
import my.mma.smtp.dto.VerifyCodeRequest;
import my.mma.smtp.service.SmtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smtp")
public class SmtpController {

    private final SmtpService smtpService;

    @PostMapping("/verification-code-transmission")
    public ResponseEntity<EmailVerificationSendResult> sendEmailVerificationCode(
            @RequestBody @Validated EmailVerificationCodeRequest request
    ) {
        return ResponseEntity.ok().body(smtpService.sendEmailVerificationCode(request));
    }

    @PostMapping("/code-verification")
    public ResponseEntity<Void> verifyCode(
            @RequestBody @Validated VerifyCodeRequest verifyCodeRequest
    ) {
        if (smtpService.verifyCode(verifyCodeRequest))
            return ResponseEntity.status(HttpStatus.OK).body(null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @PostMapping("/password-reset-token")
    public ResponseEntity<PasswordResetTokenResponse> verifyCodeAndCreateResetToken(
            @RequestBody @Validated VerifyCodeRequest verifyCodeRequest
    ) {
        if (!smtpService.verifyCode(verifyCodeRequest))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        PasswordResetTokenResponse response = smtpService.createPasswordResetToken(verifyCodeRequest.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
