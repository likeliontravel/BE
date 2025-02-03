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

        //Request 에서 쿠키 추출
        Cookie[] cookies = request.getCookies();
        String accessToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                }
            }
        }

        //Request 에서 로컬스토리지에 토큰 추출
        String refreshToken = request.getHeader("Refresh-Token");

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

            // Authorization 쿠키 삭제
            Cookie deleteCookie = new Cookie("Authorization", null);
            deleteCookie.setMaxAge(0);
            deleteCookie.setPath("/");
            response.addCookie(deleteCookie);

            // 로그아웃 성공 응답 설정
            CommonResponse<String> commonResponse = CommonResponse.success(null, "성공적으로 로그아웃 되었습니다.");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(commonResponse));

        } catch (Exception e) {

            CommonResponse<String> commonResponse = CommonResponse.error(
                    HttpStatus.BAD_REQUEST.value(), e.getMessage()
            );

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE+";charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(commonResponse));

        }
    }
}
