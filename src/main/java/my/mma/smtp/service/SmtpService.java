package my.mma.smtp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.security.entity.PasswordResetToken;
import my.mma.security.repository.PasswordResetTokenRepository;
import my.mma.smtp.dto.EmailVerificationCodeRequest;
import my.mma.smtp.dto.EmailVerificationSendResult;
import my.mma.smtp.dto.PasswordResetTokenResponse;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import my.mma.smtp.dto.VerifyCodeRequest;
import my.mma.smtp.entity.JoinCode;
import my.mma.smtp.repository.JoinCodeRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

import static my.mma.smtp.constant.JoinCodeConstant.*;
import static my.mma.smtp.dto.EmailVerificationSendResult.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SmtpService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final JoinCodeRepository joinCodeRepository;
    private final PasswordResetTokenRepository resetTokenRepository;

    @Transactional
    public EmailVerificationSendResult sendEmailVerificationCode(EmailVerificationCodeRequest request) {
        if (request.isJoin()) {
            if (userRepository.existsByEmail(request.email()))
                return EMAIL_ALREADY_EXISTS;
        } else {
            User user = userRepository.findByEmail(request.email()).orElse(null);
            if (user == null){
                return EMAIL_NOT_FOUND;
            }else if(user.getPassword() == null) {
                return SOCIAL_LOGIN_ACCOUNT;
            }
        }
        sendCode(request);
        return SUCCESS;
    }

    private void sendCode(EmailVerificationCodeRequest request) {
        String joinCode = generateRandomNumber();
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setTo(request.email());
        smm.setSubject("fightapp 회원가입 인증 코드");
        smm.setText(joinCode);
        mailSender.send(smm);
        joinCodeRepository.save(JoinCode.builder()
                .email(request.email())
                .code(joinCode)
                .expiration(EXPIRATION_SECONDS.getValue())
                .build());
    }

    private String generateRandomNumber() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    @Transactional
    public boolean verifyCode(VerifyCodeRequest verifyCodeDto) {
        JoinCode joinCode = joinCodeRepository.findById(verifyCodeDto.email()).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_EMAIL_CONFIGURED_400)
        );
        if (joinCode.getCode().equals(verifyCodeDto.code())) {
            joinCodeRepository.delete(joinCode);
            return true;
        }
        return false;
    }

    @Transactional
    public PasswordResetTokenResponse createPasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();
        resetTokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .email(email)
                .build());
        return new PasswordResetTokenResponse(token);
    }

}
