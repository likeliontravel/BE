package org.example.be.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RestLogoutHandler implements LogoutHandler {

    private final JWTUtil jwtUtil;
    private final JWTBlackListService jwtBlackListService;

    @SneakyThrows
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        ObjectMapper objectMapper = new ObjectMapper();

        String accessToken = extractTokenFromHeaderAndCookie(request, "Authorization");
        String refreshToken = extractTokenFromHeaderAndCookie(request, "Refresh-Token");

        // AccessToken 이나 RefreshToken 이 없는 경우
        if (accessToken == null || refreshToken == null) {
            CommonResponse<String> commonResponse = CommonResponse.error(
                    HttpStatus.BAD_REQUEST.value(), "Access Token or Refresh Token are missing."
            );
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE+";charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(commonResponse));
            return;
        }

        try {
            // OAuth2 사용자 확인
            if(authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                String userKey = oAuth2User.getUserIdentifier();    // OAuth2 사용자 이메일 (provider + " " + providerId)

                // OAuth2 사용자 토큰을 플랙리스트에 추가
                jwtBlackListService.addToBlackList(userKey, accessToken, refreshToken, jwtUtil.getExpiration(accessToken));

                System.out.println("OAuth2 사용자 로그아웃: " + userKey);
            } else {
                // 일반 사용자 토큰을 블랙리스트에 추가
                jwtBlackListService.addToBlackList(jwtUtil.getUserIdentifier(accessToken), accessToken, refreshToken, jwtUtil.getExpiration(accessToken));
            }

            // SecurityContext에서 인증객체 삭제
            SecurityContextHolder.clearContext();

            // Authorization, Refresh-Token 쿠키 삭제
            Cookie deleteAccessCookie = new Cookie("Authorization", null);
            deleteAccessCookie.setSecure(true);
            deleteAccessCookie.setHttpOnly(true);
            deleteAccessCookie.setMaxAge(0);
            deleteAccessCookie.setPath("/");
            response.addCookie(deleteAccessCookie);

            Cookie deleteRefreshCookie = new Cookie("Refresh-Token", null);
            deleteAccessCookie.setSecure(true);
            deleteAccessCookie.setHttpOnly(true);
            deleteAccessCookie.setMaxAge(0);
            deleteAccessCookie.setPath("/");
            response.addCookie(deleteRefreshCookie);

        } catch (Exception e) {

            CommonResponse<String> commonResponse = CommonResponse.error(
                    HttpStatus.BAD_REQUEST.value(), e.getMessage()
            );

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE+";charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(commonResponse));

        }
    }

    // 헤더에서 먼저 토큰 탐색, 발견되지 않으면 쿠키에서 탐색
    private String extractTokenFromHeaderAndCookie(HttpServletRequest request, String headerName) {
        // 헤더 선 탐색
        String token = request.getHeader(headerName);

        // 헤더에서 발견되지 않을 경우 쿠키 탐색
        if (token == null) {
            System.out.println("헤더에서 " + headerName + " 발견되지 않음. 쿠키 탐색 시작");
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(headerName)) {
                        return cookie.getValue();
                    }
                }
            }
        } else if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
