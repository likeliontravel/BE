//package org.example.be.chat.config;
//
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.example.be.jwt.service.JWTBlackListService;
//import org.example.be.jwt.util.JWTUtil;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//import java.util.List;
//import java.util.Map;
//
//@RequiredArgsConstructor
//public class JWTHandshakeInterceptor implements HandshakeInterceptor {
//
//    private final JWTUtil jwtUtil;
//    private final JWTBlackListService jwtBlackListService;
//
//    // WebSocket 연결 시 JWT 토큰 검증한 후 인증 정보를 SecurityContextHolder에 저장
//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
//
//        if (request instanceof ServletServerHttpRequest servletRequest) {
//            HttpServletRequest httpRequest = servletRequest.getServletRequest();
//
//            String accessToken = httpRequest.getParameter("accessToken");
//            String refreshToken = httpRequest.getParameter("refreshToken");
//
//            if (accessToken == null || !jwtUtil.isValid(accessToken)) {
//                System.out.println("[WebSocket] 유효하지 않은 JWT AccessToken입니다.");
//                return false;
//            }
//
//            String userIdentifier = jwtUtil.getUserIdentifier(accessToken);
//            if (userIdentifier == null || userIdentifier.isBlank()) {
//                System.out.println("[WebSocket] JWT에서 userIdentifier 추출 실패");
//                return false;
//            }
//
//            if (jwtBlackListService.isBlacklistedByUserIdentifier(userIdentifier, accessToken, refreshToken)) {
//                System.out.println("[WebSocket] 블랙리스트에 등록된 토큰입니다.");
//                return false;
//            }
//
//            // 여기까지 통과
//
//            // 인증 성공 : SecurityContextHolder에 인증 객체 등록하기
//            Authentication auth = new UsernamePasswordAuthenticationToken(
//                    userIdentifier,
//                    null,
//                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
//            );
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            System.out.println("[WebSocket] WebSocket 연결 인증 성공 - userIdentifier: " + userIdentifier);
//        }
//
//        return true;
//    }
//
//    @Override
//    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                               WebSocketHandler wsHandler, Exception exception) {
//        // 추후 후처리 필요 시 여기에 작성하면 됨
//    }
//
//}