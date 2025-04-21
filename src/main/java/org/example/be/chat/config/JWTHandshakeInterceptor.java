package org.example.be.chat.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@RequiredArgsConstructor
public class JWTHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;
    private final JWTBlackListService jwtBlackListService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        // 요청 타입이 ServletServerHttpRequest이면 헤더 또는 쿠키에서 토큰 추출
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

//            String accessToken = extractToken(httpRequest, "Authorization");
//            String refreshToken = extractToken(httpRequest, "Refresh-Token");

            String accessToken = extractTokenFromQuery(httpRequest, "accessToken");
            String refreshToken = extractTokenFromQuery(httpRequest, "refreshToken");

            if (accessToken == null || !jwtUtil.isValid(accessToken)) {
                System.out.println("[WebSocket] 유효하지 않은 JWT AccessToken.");
                return false;
            }

            String userIdentifier = jwtUtil.getUserIdentifier(accessToken);
            if (userIdentifier == null || userIdentifier.isBlank()) {
                System.out.println("[WebSocket] JWT에서 userIdentifier 추출 실패");
                return false;
            }

            // 블랙리스트 체크
            if (jwtBlackListService.isBlacklistedByUserIdentifier(userIdentifier, accessToken, refreshToken)) {
                System.out.println("[WebSocket] 블랙리스트 등록 토큰");
                return false;
            }

            // 세션에 저장
            attributes.put("userIdentifier", userIdentifier);
            System.out.println("[WS] WebSocket 연결 인증 성공 - userIdentifier: " + userIdentifier);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }



    // 헤더 또는 쿠키에서 토큰 추출
    private String extractToken(HttpServletRequest request, String headerName) {
        String token = request.getHeader(headerName);
        if (token == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(headerName)) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private String extractTokenFromQuery(HttpServletRequest request, String paramName) {
        return request.getParameter(paramName);
    }


}
