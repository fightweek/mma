package my.mma.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.security.entity.UserEntity;
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
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void joinUser(JoinDto joinDto){
        userRepository.save(
                UserEntity.builder()
                        .loginId(joinDto.getLoginId())
                        .role("ROLE_ADMIN")
                        .password(bCryptPasswordEncoder.encode(joinDto.getPassword()))
                        .name(joinDto.getUsername())
                        .build()
        );
    }

}