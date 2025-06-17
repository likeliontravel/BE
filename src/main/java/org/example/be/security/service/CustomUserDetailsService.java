package org.example.be.security.service;

import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.domain.GeneralUser;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.example.be.security.dto.UserContext;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/*
 * 사용자의 정보를 DB 에서 가져오는 서비스 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final GeneralUserRepository generalUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdentifier) throws UsernameNotFoundException {

        Optional<GeneralUser> user = generalUserRepository.findByUserIdentifier(userIdentifier);

        if (user.isEmpty()) {

            throw new UsernameNotFoundException(userIdentifier + " 해당 userIdentifier을 찾을 수 없습니다.");
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.get().getRole()));
        return new UserContext(user.get().mapToDTO(), authorities);
    }
}
