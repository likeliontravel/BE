package org.example.be.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.jwt.provider.JWTProvider;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final JWTProvider jwtProvider;
    private final JWTBlackListService jwtBlackListService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        System.out.println("JWTFilter - 요청 URL : " + request.getRequestURI());

        // 일반회원가입과 로그인 요청, 웹소켓 연결은 JWT 필터 적용 제외
        if (requestURI.equals("/general-user/signup") || requestURI.equals("/login")
                || requestURI.startsWith("/ws") || requestURI.startsWith("/mail") || requestURI.startsWith("/tourism/fetch/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 헤더에서 토큰 추출
        String accessToken = extractTokenFromHeaderAndCookie(request, "Authorization", "accessToken");
        String refreshToken = extractTokenFromHeaderAndCookie(request, "Refresh-Token", "refreshToken");

        // AccessToken이 없으면 인증 실패 처리
        if (accessToken == null || !jwtUtil.isValid(accessToken)) {
            System.out.println("AccessToken 없음");
            sendUnauthorizedResponse(response, "Access Token이 필요합니다.");
            return;
        }

        // userIdentifier 꺼내기
        String userIdentifier = jwtUtil.getUserIdentifier(accessToken);
        if (userIdentifier == null || userIdentifier.isEmpty()) {
            System.out.println("JWT에서 userIdentifier 추출 실패");
            sendUnauthorizedResponse(response, "JWT에서 userIdentifier 추출 실패");
            return;
        }

        // 블랙리스트에 등록된 토큰인지 확인
        if (jwtBlackListService.isBlacklistedByUserIdentifier(userIdentifier, accessToken, refreshToken)) {
            System.out.println("블랙리스트에 등록된 토큰");
            sendUnauthorizedResponse(response, "토큰이 블랙리스트에 등록되어 있습니다.");
            return;
        }

        // AccessToken 만료 시 검증
        if (jwtUtil.isExpired(accessToken)) {
            System.out.println("AccessToken 만료됨");

            // RefreshToken이 존재하며 유효하고 만료되지 않은 경우 AccessToken 재발급
            if (refreshToken != null && jwtUtil.isValid(refreshToken) && !jwtUtil.isExpired(refreshToken)) {
                System.out.println("Refresh토큰이 유효하여 AccessToken을 재발급합니다.");
                String role = jwtUtil.getRole(refreshToken);
                String newAccessToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60);   // 1시간

                response.addCookie(createCookie("Authorization", newAccessToken));
                response.setHeader("Authorization", "Bearer " + newAccessToken);
                accessToken = newAccessToken;
            } else {
                System.out.println("Refresh Token도 만료됨. 재로그인 필요.");
                sendUnauthorizedResponse(response, "Access Token이 만료되었으며 Refresh Token이 유효하지 않습니다.");
                return;
            }
        }

        try {
            Authentication authentication = jwtProvider.getUserDetails(userIdentifier);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("인증 성공 : " + userIdentifier);
        } catch (Exception e) {
            System.out.println("JWT 검증 실패: " + e.getMessage());
            sendUnauthorizedResponse(response, "유효하지 않은 JWT 토큰");
            return;
        }

        System.out.println("[JWTFilter 마지막] SecurityContextHolder 인증 객체: " +
                SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
    }


    // 헤더에서 먼저 토큰 탐색, 발견되지 않으면 쿠키에서 탐색
    private String extractTokenFromHeaderAndCookie(HttpServletRequest request, String headerName, String paramName) {
        // 헤더 선 탐색
        String token = request.getHeader(headerName);

        // 헤더에서 발견되지 않을 경우 쿠키 탐색
        if (token == null) {
            System.out.println("헤더에서 " + headerName + " 발견되지 않음. 쿠키 탐색 시작");
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(headerName)) {
                        return cookie.getValue();
                    }
                }
            }
        } else if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        // 쿼리 파라미터에서 탐색
        token = request.getParameter(paramName);
        if (token != null) {
            return token;
        }

        return null;
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간
        return cookie;
    }

    // 인증 실패 응답 보내기
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }

}
