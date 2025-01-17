package org.example.be.security.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


/*
 * 사용자의 기본 정보를 저장하는 인터페이스
 * 저장된 정보는 추후 인증 절차에 사용위해 Authentication 객체에 포함되어 제공 */
@Getter
@Setter
@RequiredArgsConstructor
public class UserContext implements UserDetails {

    private final GeneralUserDTO generalUserDTO;

    private final List<GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return generalUserDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return generalUserDTO.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
