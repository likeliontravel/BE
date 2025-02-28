package org.example.be.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.be.jwt.provider.JWTProvider;
import org.example.be.security.dto.LoginDTO;
import org.example.be.security.token.RestAuthenticationToken;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class RestAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JWTProvider jwtProvider;

    /*
    * 요청 URL
    * /login 일 때 작동하는 필터 */
    public RestAuthenticationFilter(JWTProvider jwtProvider) {
        super(new AntPathRequestMatcher("/login", "POST"));
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {

        if (!HttpMethod.POST.name().equals(request.getMethod())) {

            throw new IllegalArgumentException("지원되지 않는 HTTP 메소드 방식 입니다 : " + request.getMethod());
        }

        LoginDTO loginDTO = objectMapper.readValue(request.getReader(), LoginDTO.class);

        if (!StringUtils.hasText((loginDTO.getEmail())) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new AuthenticationServiceException("유저의 아이디나 비밀번호가 정상적으로 제공되지 않음");
        }

        RestAuthenticationToken restAuthenticationToken = new RestAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        return getAuthenticationManager().authenticate(restAuthenticationToken);
    }
}
