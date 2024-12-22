package org.example.be.jwt.provider;

import lombok.RequiredArgsConstructor;
import org.example.be.security.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JWTProvider {

    private final CustomUserDetailsService customUserDetailsService;

    //유저 인증객체 생성
    public Authentication getUserDetails(String username) {

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
