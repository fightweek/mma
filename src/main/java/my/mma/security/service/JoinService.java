package my.mma.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.security.entity.Member;
import my.mma.security.repository.UserRepository;
import my.mma.security.oauth2.dto.JoinDto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JoinService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // Config 클래스에 등록된 encoder bean 사용

    @Transactional
    public void joinUser(JoinDto joinDto){
        userRepository.save(
                Member.builder()
                        .email(joinDto.getEmail())
                        .role("ROLE_ADMIN") // 앞에 접두사(ROLE)를 가져야 함
                        .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
                        .name(joinDto.getUsername())
                        .build()
        );
    }

}