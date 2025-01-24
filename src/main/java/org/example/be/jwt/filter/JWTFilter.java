package org.example.be.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
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

    private final JWTBlackListService jwtBlackListService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.equals("/general-user/SignUp")) {
            filterChain.doFilter(request, response);
            return;
        }

        //Request 에서 쿠키 추출
       Cookie[] cookies = request.getCookies();

       // Request 에서 로컬스토리지 추출
        String refreshToken = request.getHeader("Refresh_token");


       // 쿠키가 없고 리프레쉬 토큰도 없으면 다음 필터로 넘어감
        if (cookies == null && (refreshToken == null || refreshToken.isEmpty())) {

            System.out.println("No cookies found or refresh token is empty");

            filterChain.doFilter(request, response);

            return;
        }

        String authorization = null;

        // 쿠키 값을 for 문을 통해 Authorization 이름을 가진 쿠키를 찾음
        if (cookies != null) {

            for (Cookie cookie : cookies) {

                if ("Authorization".equals(cookie.getName())) {

                    authorization = cookie.getValue();
                }
            }
        }

        // 토큰이 없으면 다음 필터로 넘김
        if (authorization == null) {

            System.out.println("Access Token is null");

            filterChain.doFilter(request, response);

            return;
        }

        String accessToken = authorization;

        /*
        * 토큰 블랙리스트 검증 로직 */
        if (jwtBlackListService.isBlacklistedByUserIdentifier(jwtUtil.getUserIdentifier(accessToken),accessToken, refreshToken)) {

            ObjectMapper mapper = new ObjectMapper();

            CommonResponse<String> commonResponse = CommonResponse.error(
                    HttpStatus.UNAUTHORIZED.value(), "토큰이 블랙리스트에 올라가 있습니다.");

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// 401 상태 반환

            response.getWriter().write(mapper.writeValueAsString(commonResponse));

            return;
        }

        // 토큰이 만료시간이 지나면 401 에러 보냄
        if (jwtUtil.isExpired(accessToken)) {

            System.out.println("Access Token expired");

            // 리프레시 토큰 검증 및 Access 토큰 재발급

            if (refreshToken != null && jwtUtil.isValid(refreshToken) && jwtUtil.isExpired(refreshToken)) {

                String userIdentifier = jwtUtil.getUserIdentifier(refreshToken);
                String role = jwtUtil.getRole(refreshToken);

                String newAccessToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60); // 1시간 토큰 발급

                // 새로운 Access 토큰을 쿠키에 추가
                Cookie newAccessTokenCookie = new Cookie("Authorization", newAccessToken);
                newAccessTokenCookie.setHttpOnly(true);
                newAccessTokenCookie.setPath("/");
                newAccessTokenCookie.setMaxAge(60 * 60); // 1시간 유효
                response.addCookie(newAccessTokenCookie);

            } else {

                ObjectMapper mapper = new ObjectMapper();

                CommonResponse<String> commonResponse = CommonResponse.error(
                        HttpStatus.UNAUTHORIZED.value(), "토큰 만료 시간이 지났습니다.");

                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// 401 상태 반환

                response.getWriter().write(mapper.writeValueAsString(commonResponse));

                return;
            }
        }

        try {

            String userIdentifier = jwtUtil.getUserIdentifier(accessToken);
            String role = jwtUtil.getRole(accessToken);

            if (userIdentifier != null && role != null) {

                // 유저 객체 정보 가져오기
                Authentication authentication = jwtProvider.getUserDetails(userIdentifier);

                // 유저 객체 정보 토큰에 담기
                SecurityContext context = getSecurityContext(authentication);
                SecurityContextHolder.setContext(context);
            }

            // 여러가지 오류들
        } catch (Exception e) {

            System.out.println("Invalid JWT token: " + e.getMessage());

            ObjectMapper mapper = new ObjectMapper();

            CommonResponse<String> commonResponse = CommonResponse.error(
                    HttpStatus.UNAUTHORIZED.value(), e.getMessage());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// 401 상태 반환

            response.getWriter().write(mapper.writeValueAsString(commonResponse));

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
