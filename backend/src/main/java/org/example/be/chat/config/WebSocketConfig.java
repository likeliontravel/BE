package org.example.be.chat.config;

import org.example.be.group.repository.GroupRepository;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JWTUtil jwtUtil;
	private final JWTBlackListService jwtBlackListService;
	private final GroupRepository groupRepository;

	// 클라이언트가 접속할 WebSocket 엔드포인트 등록
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.addInterceptors(new CustomHandshakeInterceptor(jwtUtil, jwtBlackListService, groupRepository))
			.setHandshakeHandler(new CustomHandshakeHandler())
			.setAllowedOrigins("https://localhost:3000", "https://localhost:5500", "https://toleave.shop")
			.withSockJS();  // SockJS fallback 지원
	}

	// STOMP 메시지 처리에 사용할 브로커 설정
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub"); // 클라이언트가 구독에 사용할 prefix
		registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트가 메시지 전송 시 사용할 prefix
	}

	//    // WebSocket 수신 채널에 커스텀 인터셉터 등록 ( 사용자 그룹 가입 여부 검증 ) -> 삭제
	//    // 더 이상 GroupMembershipChannelInterceptor, JWTHandshakeInterceptor를 사용하지 않는다.
	//    @Override
	//    public void configureClientInboundChannel(ChannelRegistration registration) {
	////        registration.interceptors(groupMembershipChannelInterceptor);
	//    }
}