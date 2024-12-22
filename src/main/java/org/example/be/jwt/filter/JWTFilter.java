package org.example.be.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.jwt.JWTUtil;
import org.example.be.jwt.provider.JWTProvider;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Request 에서 쿠키 추출
       Cookie[] cookies = request.getCookies();

       // 쿠키가 없으면 다음 필터로 넘어감
        if (cookies == null) {

            System.out.println("No cookies found");

            filterChain.doFilter(request, response);

            return;
        }

        String authorization = null;

        // 쿠키 값을 for 문을 통해 Authorization 이름을 가진 쿠키를 찾음
        for (Cookie cookie : cookies) {

            if ("Authorization".equals(cookie.getName())) {

                authorization = cookie.getValue();
            }
        }

        // 토큰이 없으면 다음 필터로 넘김
        if (authorization == null) {

            System.out.println("Token null");

            filterChain.doFilter(request, response);

            return;
        }

        String token = authorization;

        /*
        * 토큰 블랙리스트 기능을 만든다면
        * 여기에 추가를 해주시면 감사하겠습니다 */

        // 토큰이 만료시간이 지나면 401 에러 보냄
        if (jwtUtil.isExpired(token)) {

            CommonResponse<String> commonResponse = CommonResponse.error(
                    HttpStatus.UNAUTHORIZED.value(), "토큰 만료 시간이 지났습니다.");

            System.out.println("Token expired");

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// 401 상태 반환

            response.getWriter().write(commonResponse.toString());
            return;
        }

        try {
            String email = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            if (email != null && role != null) {

                // 유저 객체 정보 가져오기
                Authentication authentication = jwtProvider.getUserDetails(token);

                // 유저 객체 정보 토큰에 담기
                SecurityContext context = getSecurityContext(authentication);
                SecurityContextHolder.setContext(context);
            }

        } catch (Exception e) {

            System.out.println("Invalid JWT token: " + e.getMessage());

            CommonResponse<String> commonResponse = CommonResponse.error(
                    HttpStatus.UNAUTHORIZED.value(), e.getMessage());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// 401 상태 반환

            response.getWriter().write(commonResponse.toString());

            return;
        }

        filterChain.doFilter(request, response);
    }

    private static SecurityContext getSecurityContext(Authentication authentication) {

        // 권한 설정을 위한 시큐리티 컨텍스트 홀더에 유저 객체 정보 저장
        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
