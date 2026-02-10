package org.example.be.chat.config;

import java.io.IOException;
import java.util.Map;

import org.example.be.group.repository.GroupRepository;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

// 설계 변경 - GroupMembershipChannelInterceptor, JWTHandshakeInterceptor 제거

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

		// if (jwtBlacklistService.isBlacklistedByUserIdentifier(userIdentifier, accessToken, refreshToken)) {
		//     System.out.println("[WebSocket] 다시 로그인하여 시도해보세요. token is blacklisted.");
		//     return false;
		// }

		boolean isMember = groupRepository.findWithMembersByGroupName(groupName)
			.map(group -> group.getMembers().stream()
				.map(UnifiedUser::getUserIdentifier)
				.anyMatch(identifier -> identifier.equals(userIdentifier)))
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

	// 아래는 이전 CustomHandshakeHandler extends DefaultHandshakeHandler일 때의 코드. 주석처리.
	//    @Override
	//    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
	//        // 모클래스의 ServerHttpRequest는 추상적인 상위 타입임. HTTP와 Websocket을 둘 다 처리 가능하게 추상화된 ServerHttpRequest를 제공하기 때문에
	//        // 핸드셰이크 시작은 HTTP 요청으로 시작되므로, 내부적으로는 ServletServerHttpRequest가 필요함. (JWT를 꺼내야 하므로 서블릿 레벨의 HTTP요청 객체가 필요함)
	//        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest(); // 받을 타입이 명시적으로 정해져있기 때문에 var타입으로 저장해도 된다고 함..근데 난 싫어서 중복이어도 타입 명확히 써놓음
	//        String accessToken = servletRequest.getParameter("accessToken");
	//        String refreshToken = servletRequest.getParameter("refreshToken");
	//        String groupName = servletRequest.getParameter("groupName");
	//
	//        if (!jwtUtil.isValid(accessToken)) {
	//            throw new BadCredentialsException("다시 로그인하여 시도해보세요. invalid access token.");
	//        }
	//
	//        String userIdentifier = jwtUtil.getUserIdentifier(accessToken);
	//
	//        if (jwtBlacklistService.isBlacklistedByUserIdentifier(userIdentifier, accessToken, refreshToken)) {
	//            throw new BadCredentialsException("다시 로그인하여 시도해보세요. token is blacklisted.");
	//        }
	//
	//        boolean isMember = groupRepository.findWithMembersByGroupName(groupName)
	//                .map(group -> group.getMembers().stream()
	//                        .map(UnifiedUser::getUserIdentifier)
	//                        .anyMatch(identifier -> identifier.equals(userIdentifier)))
	//                .orElse(false);
	//
	//        if (!isMember) {
	//            throw new BadCredentialsException("그룹의 멤버가 아닙니다. not a member of group.");
	//        }
	//
	//        return () -> userIdentifier;    // Principal 구현체로 반환.

	//

	//    }
}

