package org.example.be.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.example.be.oauth.dto.SocialUserDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            System.out.println("No cookies found");
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = null;
        for (Cookie cookie : cookies) {
            if ("Authorization".equals(cookie.getName())) {
                authorization = cookie.getValue();
            }
        }

        if (authorization == null) {
            System.out.println("Token null");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization;

        if (jwtUtil.isExpired(token)) {
            System.out.println("Token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 반환
            return;
        }

        try {
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            SocialUserDTO socialUserDTO = new SocialUserDTO();
            socialUserDTO.setUsername(username);
            socialUserDTO.setRole(role);

            CustomOAuth2User customOAuth2User = new CustomOAuth2User(socialUserDTO);
            Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (Exception e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 반환
            return;
        }

        filterChain.doFilter(request, response);
    }
}
