package org.example.be.jwt.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.jwt.provider.JWTProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final JWTProvider jwtProvider;
    private final JWTBlackListService jwtBlackListService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // AccessToken은 쿠키에서 추출
        String accessToken = extractTokenFromCookies(httpRequest, "Authorization");
        // RefreshToken은 헤더에서 추출
        String refreshToken = httpRequest.getHeader("Refresh-Token");

        try {
            if (accessToken != null && jwtUtil.validateToken(accessToken)) {
                setSecurityContext(accessToken);
            } else if (refreshToken != null && jwtUtil.validateToken(refreshToken) && !jwtBlackListService.isBlackListed(refreshToken)) {
                // Refresh Token이 유효하면 새로운 Access Token 발급
                String userIdentifier = jwtUtil.getUserIdentifier(refreshToken);
                String role = jwtUtil.getRole(refreshToken);
                String newAccessToken = jwtProvider.generateAccessToken(userIdentifier, role);

                // 새로운 AccessToken을 쿠키에 저장
                Cookie newAccessTokenCookie = createCookie("Authorization", newAccessToken);
                httpResponse.addCookie(newAccessTokenCookie);

                // SecurityContext 업데이트
                setSecurityContext(newAccessToken);
            } else {
                throw new SecurityException("유효하지 않은 인증 토큰");
            }
        }catch (ExpiredJwtException e) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.");
            return;
        } catch (SignatureException e) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "서명이 올바르지 않은 토큰입니다.");
            return;
        } catch (MalformedJwtException e) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "손상된 토큰입니다.");
            return;
        } catch (UnsupportedJwtException e) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "지원되지 않는 토큰 형식입니다.");
            return;
        } catch (SecurityException e) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        } catch (Exception e) {
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 내부 오류 발생");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // 쿠키에서 토큰 추출하기
    private String extractTokenFromCookies(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setSecurityContext(String token) {
        try {
            Authentication authentication = jwtUtil.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();   // 예외 발생 시 보안 유지
        }
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 1);   // 유효기간 1일
        cookie.setDomain("toleave.shop");
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }

}
