package org.example.be.chat.config;

import java.io.IOException;
import java.util.Map;

import org.example.be.group.repository.GroupRepository;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

// 이 HandshakeInterceptor는 웹소켓 생성 전에 우리 회원인지 검증, 해당 그룹 멤버인지 검증하고 세션 attributes에 userIdentifier정보를 저장시켜준다.
@RequiredArgsConstructor
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

	private final JWTUtil jwtUtil;
	private final JWTBlackListService jwtBlacklistService;
	private final GroupRepository groupRepository;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) {

		HttpServletRequest servletRequest = ((ServletServerHttpRequest)request).getServletRequest();
		String accessToken = servletRequest.getParameter("accessToken");
		String refreshToken = servletRequest.getParameter("refreshToken");
		String groupName = servletRequest.getParameter("groupName");

		if (!jwtUtil.isValid(accessToken)) {
			System.out.println("[WebSocket] 다시 로그인하여 시도해보세요. invalid access token.");
			if (response instanceof ServletServerHttpResponse servletResponse) {
				servletResponse.getServletResponse().setStatus(HttpStatus.UNAUTHORIZED.value());

				try {
					servletResponse.getServletResponse()
						.getWriter()
						.write("[WebSocket] 다시 로그인하여 시도해보세요. invalid access token.");
					servletResponse.getServletResponse().flushBuffer();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			return false;
		}

		String userIdentifier = jwtUtil.getUserIdentifier(accessToken);

		// Chat 도메인 마이그레이션 시 userIdentifier 관련 전부 없앨 예정( 임시 컴파일 오류 방지용 땜빵만 놓습니다. 리팩토링할 때 userIdentifier 아예 안쓰게 바꿔용 )
		boolean isMember = groupRepository.findWithMembersByGroupName(groupName)
			.map(group -> group.getMembers().stream()
				.anyMatch(groupMember -> groupMember.getEmail().equals(userIdentifier)))
			.orElse(false);

		if (!isMember) {
			System.out.println("[WebSocket] 그룹의 멤버가 아니어서 채팅방에 입장할 수 없습니다. not a member of group.");
			if (response instanceof ServletServerHttpResponse servletResponse) {
				servletResponse.getServletResponse().setStatus(HttpStatus.FORBIDDEN.value());

				try {
					servletResponse.getServletResponse()
						.getWriter()
						.write("[WebSocket] 그룹의 멤버가 아니어서 채팅방에 입장할 수 없습니다. not a member of group.");
					servletResponse.getServletResponse().flushBuffer();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			return false;
		}

		// 인증된 사용자 정보를 WebSocket 세션에 저장
		attributes.put("userIdentifier", userIdentifier);

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {
		// 추후 필요하면 핸드셰이크 성공 후 실행할 로직 여기 작성 가능
	}

}

