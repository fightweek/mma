package my.mma.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.security.CustomUserDetails;
import my.mma.security.entity.Member;
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
        //DB에서 조회
        Optional<Member> findUser = userRepository.findByEmail(email);
        //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
//        if (userData.isPresent()) {
//            //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
//            return new CustomUserDetails(userData.get());
//        }
//        return null;
        return findUser.map(CustomUserDetails::new).orElse(null);
    }
}