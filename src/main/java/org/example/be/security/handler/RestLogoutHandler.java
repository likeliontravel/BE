package org.example.be.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
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
        String refreshToken = request.getHeader("Refresh_token");

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

            jwtBlackListService.addToBlackList(jwtUtil.getUsername(accessToken), accessToken, refreshToken, jwtUtil.getExpiration(accessToken));

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
