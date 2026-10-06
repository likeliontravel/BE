package org.example.be.domain.chat.interceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.example.be.domain.group.exception.GroupErrorCode;
import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.domain.member.exception.MemberErrorCode;
import org.example.be.domain.member.service.AuthTokenService;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.exception.support.ErrorResponseWriter;
import org.example.be.global.security.config.SecurityUser;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 이 HandshakeInterceptor는 웹소켓 생성 전에 우리 회원인지 검증, 해당 그룹 멤버인지 검증하고 세션 attributes에 SecurityUser 정보를 저장시켜준다.
@RequiredArgsConstructor
@Slf4j
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

	private final AuthTokenService authTokenService;
	private final GroupRepository groupRepository;
	private final ErrorResponseWriter errorResponseWriter;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) {

		HttpServletRequest servletRequest = ((ServletServerHttpRequest)request).getServletRequest();
		String accessToken = extractCookie(servletRequest, "accessToken");
		String groupName = servletRequest.getParameter("groupName");

		log.debug("[WebSocket Debug] Handshake attempt - groupName: {}", groupName);

		// 1. 토큰 검증 및 클레임 추출
		Map<String, Object> claims = authTokenService.payload(accessToken);
		if (claims == null) {
			log.debug("[WebSocket Debug] Token validation failed");
			return failHandshake(response, MemberErrorCode.INVALID_TOKEN);
		}

		long memberId = ((Number)claims.get("id")).longValue();
		String email = (String)claims.get("email");
		String name = (String)claims.get("name");
		String role = (String)claims.get("role");

		log.debug("[WebSocket Debug] User authenticated - memberId: {}, email: {}", memberId, email);

		// 2. SecurityUser 객체 생성
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
		SecurityUser securityUser = new SecurityUser(
			memberId, email, "", name, List.of(authority)
		);

		// 3. 해당 그룹의 멤버인지 검증 (ID 기반)
		boolean isMember = groupRepository.existsByGroupNameAndMembers_Id(groupName, memberId);

		if (!isMember) {
			log.debug("[WebSocket Debug] User {} is NOT a member of group {} or group not found", memberId, groupName);
			return failHandshake(response, GroupErrorCode.GROUP_ACCESS_DENIED);
		}

		// 4. 인증된 사용자 정보를 WebSocket 세션 속성에 저장 (HandshakeHandler에서 Principal로 변환 예정)
		attributes.put("securityUser", securityUser);
		log.debug("[WebSocket Debug] Handshake successful");

		return true;
	}

	private String extractCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null) {
			return null;
		}
		return Arrays.stream(request.getCookies())
			.filter(cookie -> cookie.getName().equals(name))
			.map(jakarta.servlet.http.Cookie::getValue)
			.findFirst()
			.orElse(null);
	}

	// 핸드셰이크를 거부하고 사유를 CommonResponse 규격 JSON 으로 응답한다.
	// 거부는 예외가 아니라 false 반환하고, SockJS 핸들러는 컨트롤러 메서드가 아니라 advice 도 적용되지 않으므로
	// 응답을 여기서 직접 써야 한다. ErrorResponseWriter 를 거치며 상태코드도 ErrorCode 가 정한다.
	private boolean failHandshake(ServerHttpResponse response, ErrorCode errorCode) {
		if (response instanceof ServletServerHttpResponse servletResponse) {
			try {
				errorResponseWriter.write(servletResponse.getServletResponse(), errorCode);
			} catch (IOException e) {
				// 거부 응답조차 쓸 수 없는 상태(클라이언트 이탈 등). 핸드셰이크는 어차피 거부되므로 기록만 남긴다. 이 때에는 스택 포함.
				log.warn("[Handshake] 거부 응답 작성 실패 - code={}", errorCode.name(), e);
			}
		}
		return false;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {
	}
}

