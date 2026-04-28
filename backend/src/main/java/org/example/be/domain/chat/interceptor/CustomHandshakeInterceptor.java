package org.example.be.domain.chat.interceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.domain.member.service.AuthTokenService;
import org.example.be.global.security.config.SecurityUser;
import org.springframework.http.HttpStatus;
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

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) {

		HttpServletRequest servletRequest = ((ServletServerHttpRequest)request).getServletRequest();
		String accessToken = servletRequest.getParameter("accessToken");
		String groupName = servletRequest.getParameter("groupName");

		log.debug("[WebSocket Debug] Handshake attempt - groupName: {}", groupName);

		// 1. 토큰 검증 및 클레임 추출
		Map<String, Object> claims = authTokenService.payload(accessToken);
		if (claims == null) {
			log.debug("[WebSocket Debug] Token validation failed");
			return failHandshake(response, "유효하지 않거나 만료된 토큰입니다.");
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
			return failHandshake(response, "해당 그룹의 멤버가 아닙니다.");
		}

		// 4. 인증된 사용자 정보를 WebSocket 세션 속성에 저장 (HandshakeHandler에서 Principal로 변환 예정)
		attributes.put("securityUser", securityUser);
		log.debug("[WebSocket Debug] Handshake successful");

		return true;
	}

	private boolean failHandshake(ServerHttpResponse response, String message) {
		if (response instanceof ServletServerHttpResponse servletResponse) {
			servletResponse.getServletResponse().setStatus(HttpStatus.FORBIDDEN.value());
			try {
				servletResponse.getServletResponse().getWriter().write(message);
				servletResponse.getServletResponse().flushBuffer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {
	}
}

