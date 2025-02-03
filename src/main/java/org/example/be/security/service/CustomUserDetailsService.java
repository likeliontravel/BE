package org.example.be.security.service;

import lombok.RequiredArgsConstructor;
import org.example.be.security.dto.UserContext;
import org.example.be.generaluser.domain.GeneralUser;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.example.be.generaluser.repository.GeneralUserRepository;
import org.example.be.unifieduser.dto.UnifiedUserDTO;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
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

    private final UnifiedUserRepository unifiedUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdentifier) throws UsernameNotFoundException {

        Optional<UnifiedUser> userOptional = unifiedUserRepository.findByUserIdentifier(userIdentifier);

        if (userOptional.isEmpty()) {

            throw new UsernameNotFoundException(userIdentifier + " 해당 userIdentifier을 찾을 수 없습니다.");
        }

        UnifiedUser user = userOptional.get();

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));

        // UnifiedUserDTO 생성 (필요에 따라 DTO 필드를 조정)
        UnifiedUserDTO unifiedUserDTO = new UnifiedUserDTO();
        unifiedUserDTO.setId(user.getId());
        unifiedUserDTO.setEmail(user.getEmail());
        // 일반 로그인 시 패스워드가 있다면 설정, OAuth2 로그인은 패스워드가 없을 수 있으므로 상황에 맞게 처리합니다.
        unifiedUserDTO.setPassword(user.getPassword() != null ? user.getPassword() : "");
        unifiedUserDTO.setName(user.getName());
        unifiedUserDTO.setRole(user.getRole());
        unifiedUserDTO.setUserIdentifier(user.getUserIdentifier());

        return new UserContext(unifiedUserDTO, authorities);

    }
}
