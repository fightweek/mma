package my.mma.security.service;

import lombok.extern.slf4j.Slf4j;
import my.mma.security.CustomUserDetails;
import my.mma.security.entity.UserEntity;
import my.mma.security.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        //DB에서 조회
        Optional<UserEntity> findUser = userRepository.findByLoginId(loginId);
        //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
//        if (userData.isPresent()) {
//            //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
//            return new CustomUserDetails(userData.get());
//        }
//        return null;
        return findUser.map(CustomUserDetails::new).orElse(null);
    }
}