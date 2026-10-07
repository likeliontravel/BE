package org.example.be.global.config;

import org.example.be.domain.chat.handler.CustomHandshakeHandler;
import org.example.be.domain.chat.interceptor.CustomHandshakeInterceptor;
import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.domain.member.service.AuthTokenService;
import org.example.be.global.exception.support.ErrorResponseWriter;
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

	private final AuthTokenService authTokenService;
	private final GroupRepository groupRepository;
	private final ErrorResponseWriter errorResponseWriter;

	// 클라이언트가 접속할 WebSocket 엔드포인트 등록
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.addInterceptors(new CustomHandshakeInterceptor(authTokenService, groupRepository, errorResponseWriter))
			.setHandshakeHandler(new CustomHandshakeHandler())
			.setAllowedOrigins("https://localhost:3000", "https://localhost:5500", "https://toleave.cloud")
			.withSockJS();  // SockJS fallback 지원
	}

	// STOMP 메시지 처리에 사용할 브로커 설정
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// "/queue" 는 사용자별 오류 통보 채널 ('/user/queue/errors' - ChatSocketExceptionHandler) 을 위한 것이다.
		// '/user/queue/errors' 는 '/queue/errors-user{세션id}' 로 바뀌어 이 브로커로 오는데,
		// 브로커는 등록된 prefix 로 시작하지 않는 목적지를 예외 없이 버린다. 즉 이 값을 빼면 오류 메시지만 조용히 사라진다.
		// (컴파일, 부팅, 채팅 브로드캐스트는 모두 정상이라 눈에 보이지 않는다. 이 계약은 ChatSocketExceptionHandlerIT 테스트가 고정한다)
		registry.enableSimpleBroker("/sub", "/queue"); // 클라이언트가 구독에 사용할 prefix
		registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트가 메시지 전송 시 사용할 prefix
	}

	//    // WebSocket 수신 채널에 커스텀 인터셉터 등록 ( 사용자 그룹 가입 여부 검증 ) -> 삭제
	//    // 더 이상 GroupMembershipChannelInterceptor, JWTHandshakeInterceptor를 사용하지 않는다.
	//    @Override
	//    public void configureClientInboundChannel(ChannelRegistration registration) {
	////        registration.interceptors(groupMembershipChannelInterceptor);
	//    }
}