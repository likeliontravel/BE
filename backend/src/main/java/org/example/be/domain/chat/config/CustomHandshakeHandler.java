package org.example.be.domain.chat.config;

import java.security.Principal;
import java.util.Map;

import org.example.be.global.security.config.SecurityUser;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
		Map<String, Object> attributes) {

		SecurityUser securityUser = (SecurityUser)attributes.get("securityUser");

		if (securityUser == null) {
			return null;
		}

		// SecurityUser를 담은 Authentication 객체 반환 ( @AuthenticationPrincipal 연동을 위함 )
		return new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
	}
}
