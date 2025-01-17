package org.example.be.security.provider;

import lombok.RequiredArgsConstructor;
import org.example.be.security.dto.UserContext;
import org.example.be.security.service.CustomUserDetailsService;
import org.example.be.security.token.RestAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*
 * AuthenticationProvider
 * 위에 Provider 대신 사용할 CustomProvider
 * 실제 인증을 수행하는 클래스 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService customUserDetailsService;

    private final PasswordEncoder passwordEncoder;

    /*
     * Authentication 반환
     * 객체에는 사용자의 신원 정보와 인증된 자격 증명 포함 */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String email = authentication.getName();
        String password = (String) authentication.getCredentials();

        // userDetailsService 사용하여 loadUserByName 으로 해당 유저가 있는지 확인
        UserContext userContext = (UserContext) customUserDetailsService.loadUserByUsername(email);

        //실제 인증하는 비밀번호가 일치 하는지 인증하는 코드
        if (!passwordEncoder.matches(password, userContext.getPassword())) {
            throw new BadCredentialsException("비밀번호가 틀립니다.");
        }

        //Authentication 토큰을 만들어 권한 및 User 각종 정보를 반환 비밀번호는 null 반환
        return new RestAuthenticationToken(userContext.getAuthorities(), userContext.getGeneralUserDTO(), null);
    }

    /*
     * supports 값이 false 이면 여기있는 Provider 실행이 되지 않음
     * authentication 형식이 RestAuthenticationToken 클래스 하고 같은지 확인 */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(RestAuthenticationToken.class);
    }
}
