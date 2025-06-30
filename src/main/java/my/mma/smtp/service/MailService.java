package my.mma.smtp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.user.repository.UserRepository;
import my.mma.smtp.dto.VerifyCodeRequest;
import my.mma.smtp.entity.JoinCode;
import my.mma.smtp.repository.JoinCodeRepository;
import my.mma.user.entity.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final JoinCodeRepository joinCodeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public boolean sendJoinCode(
            Map<String, String> emailTo
    ) {
        String email = emailTo.get("emailTo");
        log.info("email = {}", email);
        if (userRepository.findByEmail(email).isEmpty()) {
            String joinCode = generateRandomNumber();
            SimpleMailMessage smm = new SimpleMailMessage();
            smm.setTo(email);
            smm.setSubject("fightapp 회원가입 인증 코드");
            smm.setText(joinCode);
            mailSender.send(smm);
            joinCodeRepository.save(JoinCode.builder()
                    .email(email)
                    .code(joinCode)
                    .expiration(300)
                    .build());
            return true;
        }
        return false;
    }

    private String generateRandomNumber() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++)
            sb.append(random.nextInt(10));
        return sb.toString();
    }

    @Transactional
    public boolean verifyCode(VerifyCodeRequest verifyCodeDto) {
        JoinCode joinCode = joinCodeRepository.findById(verifyCodeDto.getEmail()).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_EMAIL_CONFIGURED_500)
        );
        if (joinCode.getCode().equals(verifyCodeDto.getCode())) {
            joinCodeRepository.delete(joinCode);
            userRepository.save(
                    User.builder()
                            .role("ROLE_USER")
                            .email(verifyCodeDto.getEmail())
                            .password(bCryptPasswordEncoder.encode(verifyCodeDto.getPassword()))
                            .nickname(verifyCodeDto.getNickname())
                            .build()
            );
            return true;
        }
        return false;
    }

}
