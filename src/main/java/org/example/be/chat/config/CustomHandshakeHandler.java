package org.example.be.chat.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        String userIdentifier = (String) attributes.get("userIdentifier");
        return () -> userIdentifier;

//        // 위 리턴구문 작동 형태
//        return new Principal() {
//            @Override
//            public String getName() {
//                return userIdentifier;
//            }
//        };
    }

}
