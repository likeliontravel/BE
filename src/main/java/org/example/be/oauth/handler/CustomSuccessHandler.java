package org.example.be.oauth.handler;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String userIdentifier = customUserDetails.getUserIdentifier();
        String role = customUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_USER");

        // AccessToken, RefreshToken 발급
        String accessToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60); //1시간
        String refreshToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60 * 24 * 7); // 7일 유효

        System.out.println("로그인 성공: " + userIdentifier);
        System.out.println("생성된 accessToken 토큰 : " + accessToken);
        System.out.println("생성된 refreshToken 토큰 : " + refreshToken);

        // 쿠키, 헤더 각각 추가
        response.addCookie(createCookie("Authorization", accessToken));
        response.addCookie(createCookie("Refresh-Token", refreshToken));
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh-Token", refreshToken);

        // SecurityContext에 인증 정보 저장하기
        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        response.sendRedirect("http://localhost:3000");
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60);
        cookie.setSecure(true); // https 적용 시 주석 해제할 것
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        //System.out.println("Cookie: " + cookie.getValue());

        return cookie;
    }
}
