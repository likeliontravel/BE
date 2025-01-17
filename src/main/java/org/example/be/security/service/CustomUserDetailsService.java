package org.example.be.security.service;

import lombok.RequiredArgsConstructor;
import org.example.be.security.dto.UserContext;
import org.example.be.generaluser.domain.GeneralUser;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.example.be.generaluser.repository.GeneralUserRepository;
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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<GeneralUser> user = generalUserRepository.findByEmail(email);

        if (user.isEmpty()) {

            throw new UsernameNotFoundException(email + " 해당 이메일을 찾을 수 없습니다.");
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.get().getRole()));

        GeneralUserDTO generalUserDTO = new GeneralUserDTO();

        generalUserDTO.setId(user.get().getId());
        generalUserDTO.setEmail(user.get().getEmail());
        generalUserDTO.setPassword(user.get().getPassword());
        generalUserDTO.setName(user.get().getName());
        generalUserDTO.setRole(user.get().getRole());

        return new UserContext(generalUserDTO, authorities);
    }
}
