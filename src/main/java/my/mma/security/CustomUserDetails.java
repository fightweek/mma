package my.mma.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import my.mma.security.oauth2.dto.TempUserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

// 일반 로그인 시 사용됨
@Setter @Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final TempUserDto user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add((GrantedAuthority) user::getRole);
        return collection;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

}