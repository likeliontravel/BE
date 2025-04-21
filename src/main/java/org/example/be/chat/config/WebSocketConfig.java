package org.example.be.chat.config;

import lombok.RequiredArgsConstructor;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JWTUtil jwtUtil;
    private final JWTBlackListService jwtBlackListService;

    // 클라이언트가 연결할 WebSocket 엔드포인트 지정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket 연결 주소 : ws://localhost:8080/ws
                .addInterceptors(new JWTHandshakeInterceptor(jwtUtil, jwtBlackListService)) // jwt 인증을 위해 커스텀 핸드셰이크 인터셉터 추가
                .setAllowedOrigins("https://localhost:5500") // 배포 시 CORS 정확한 지정 필요
                .withSockJS();  // SockJS fallback 지원 (웹소켓 미지원 브라우저일 경우)
    }

    // 메시지 브로커 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독하는 prefix
        registry.enableSimpleBroker("/sub");

        // 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
