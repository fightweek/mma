package my.mma.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.security.CustomUserDetails;
import my.mma.user.entity.User;
import my.mma.security.oauth2.dto.TempUserDto;
import my.mma.security.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> findUser = userRepository.findByEmailAndUsernameIsNull(email);
        //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
        return findUser.map(user -> new CustomUserDetails(toDto(user))).orElse(null);
    }

    private TempUserDto toDto(User user) {
        return TempUserDto.builder()
                .role(user.getRole())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .build();
    }
}