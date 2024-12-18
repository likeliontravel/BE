package org.example.be.security.service;

import lombok.RequiredArgsConstructor;
import org.example.be.security.dto.UserContext;
import org.example.be.user.domain.User;
import org.example.be.user.dto.UserDTO;
import org.example.be.user.repository.UserRepository;
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

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByUserEmail(email);

        if (user.isEmpty()) {

            throw new UsernameNotFoundException(email + " 해당 이메일을 찾을 수 없습니다.");
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.get().getUserRole()));

        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.get().getId());
        userDTO.setEmail(user.get().getUserEmail());
        userDTO.setPassword(user.get().getUserPwd());
        userDTO.setName(user.get().getUserName());
        userDTO.setRole(user.get().getUserRole());
        userDTO.setPolicy(user.get().getUserPolicy());
        userDTO.setSubscribe(user.get().getUserSubscribe());

        return new UserContext(userDTO, authorities);
    }
}
