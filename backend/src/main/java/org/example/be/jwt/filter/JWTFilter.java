package org.example.be.jwt.filter;

import java.io.IOException;

import org.example.be.jwt.provider.JWTProvider;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.response.CommonResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

	private final JWTUtil jwtUtil;
	private final JWTProvider jwtProvider;
	private final JWTBlackListService jwtBlackListService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String requestURI = request.getRequestURI();
		System.out.println("JWTFilter - 요청 URL : " + request.getRequestURI());

		String requestHttpMethod = request.getMethod();
		System.out.println("JWTFilter - 요청 HTTP Method + URI : [" + requestHttpMethod + "] " + requestURI);

		// 일반회원가입과 로그인 요청, 웹소켓 연결은 JWT 필터 적용 제외
		if (requestURI.equals("/general-user/signup") || requestURI.equals("/login")
			|| requestURI.startsWith("/ws")
			|| requestURI.startsWith("/mail")
			|| requestURI.startsWith("/tourism/fetch/")
			|| requestURI.startsWith("/tourism/refresh/")
			|| requestURI.startsWith("/places")
			|| requestURI.startsWith("/schedule/get/")
			|| requestURI.startsWith("/board/search")
			|| requestURI.startsWith("/board/all")
			|| requestURI.startsWith("/board/byTheme")
			|| requestURI.startsWith("/board/byRegion")
			|| ("GET".equals(requestHttpMethod) && requestURI.startsWith("/board/"))
			|| ("GET".equals(requestHttpMethod) && requestURI.startsWith("/comment/"))
		) {
			filterChain.doFilter(request, response);
			return;
		}

		// 헤더에서 토큰 추출
		String accessToken = extractTokenFromHeaderAndCookie(request, "Authorization", "accessToken");
		String refreshToken = extractTokenFromHeaderAndCookie(request, "Refresh-Token", "refreshToken");

		// AccessToken이 없으면 인증 실패 처리
		if (accessToken == null) {
			System.out.println("AccessToken 없음");
			sendUnauthorizedResponse(response, "Access Token이 필요합니다.");
			return;
		}

		// 임시 추가 - 액세스토큰 정상 진입 여부 확인
		if (accessToken != null) {
			System.out.println("들어온 액세스 토큰 : " + accessToken);
		}

		String userIdentifier = null;

		try {
			if (jwtUtil.isValid(accessToken)) {
				if (jwtUtil.isExpired(accessToken)) {
					System.out.println("AccessToken 만료됨 - RefreshToken 검증 시작");

					if (refreshToken != null && jwtUtil.isValid(refreshToken) && !jwtUtil.isExpired(refreshToken)) {
						userIdentifier = jwtUtil.getUserIdentifier(refreshToken);
						String role = jwtUtil.getRole(refreshToken);

						String newAccessToken = jwtUtil.createJwt(userIdentifier, role,
							1000L * 60 * 60);    // 새 AccessToken 유효기간 2분
						response.addCookie(createCookie("Authorization", newAccessToken));
						response.setHeader("Authorization", "Bearer " + newAccessToken);
						accessToken = newAccessToken;

						System.out.println("RefreshToken 유효 - AccessToken 재발급 완료");
					} else {
						System.out.println("RefreshToken 만료됨. 재로그인 필요");
						sendUnauthorizedResponse(response, "재로그인이 필요합니다.");
						return;
					}
				}
				// 만료되지 않았다면 AccessToken을 그대로 사용
				userIdentifier = jwtUtil.getUserIdentifier(accessToken);
			} else {
				System.out.println("AccessToken이 유효하지 않음");
				sendUnauthorizedResponse(response, "유효하지 않은 AccessToken");
				return;
			}

			if (userIdentifier == null || userIdentifier.isEmpty()) {
				System.out.println("JWT에서 userIdentifier 추출 실패");
				sendUnauthorizedResponse(response, "유효하지 않은 AccessToken");
				return;
			}

			if (jwtBlackListService.isBlacklistedByUserIdentifier(userIdentifier, accessToken, refreshToken)) {
				System.out.println("Blacklisted Token: " + accessToken);
				sendUnauthorizedResponse(response, "재로그인이 필요합니다.");
				return;
			}

			// 인증객체 등록
			Authentication authentication = jwtProvider.getUserDetails(userIdentifier);
			SecurityContextHolder.getContext().setAuthentication(authentication);

			System.out.println("인증 성공: " + userIdentifier);
			System.out.println("[JWTFilter 마지막] SecurityContextHolder 인증 객체: " + SecurityContextHolder.getContext()
				.getAuthentication());

			filterChain.doFilter(request, response);
		} catch (Exception e) {
			System.out.println("JWT 검증 실패 : " + e.getMessage());
			sendUnauthorizedResponse(response, "JWT 인증 실패 - 서버 내부 에러");
		}
	}
	//================================ 로직 순서 변경 =================================
	//        // AccessToken이 있으나 유효하지 않은 경우 실패 처리
	//        if (!jwtUtil.isValid(accessToken)) {
	//            System.out.println("AccessToken 형식이 유효하지 않음");
	//            sendUnauthorizedResponse(response, "유효하지 않은 AccessToken");
	//            return;
	//        }
	//
	//        // userIdentifier 꺼내기
	//        String userIdentifier = jwtUtil.getUserIdentifier(accessToken);
	//        if (userIdentifier == null || userIdentifier.isEmpty()) {
	//            System.out.println("JWT에서 userIdentifier 추출 실패");
	//            sendUnauthorizedResponse(response, "JWT에서 userIdentifier 추출 실패");
	//            return;
	//        }
	//
	//        // 블랙리스트에 등록된 토큰인지 확인
	//        if (jwtBlackListService.isBlacklistedByUserIdentifier(userIdentifier, accessToken, refreshToken)) {
	//            System.out.println("블랙리스트에 등록된 토큰");
	//            sendUnauthorizedResponse(response, "토큰이 블랙리스트에 등록되어 있습니다.");
	//            return;
	//        }
	//
	//        // AccessToken 만료 시 검증
	//        if (jwtUtil.isExpired(accessToken)) {
	//            System.out.println("AccessToken 만료됨");
	//
	//            // RefreshToken이 존재하며 유효하고 만료되지 않은 경우 AccessToken 재발급
	//            if (refreshToken != null && jwtUtil.isValid(refreshToken) && !jwtUtil.isExpired(refreshToken)) {
	//                System.out.println("Refresh토큰이 유효하여 AccessToken을 재발급합니다.");
	//                String role = jwtUtil.getRole(refreshToken);
	//                String newAccessToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 2);   // 1시간(1000L * 60 * 60), 2분(1000L * 60 * 2)
	//
	//                response.addCookie(createCookie("Authorization", newAccessToken));
	//                response.setHeader("Authorization", "Bearer " + newAccessToken);
	//                accessToken = newAccessToken;
	//            } else {
	//                System.out.println("Refresh Token도 만료됨. 재로그인 필요.");
	//                sendUnauthorizedResponse(response, "Access Token이 만료되었으며 Refresh Token이 유효하지 않습니다.");
	//                return;
	//            }
	//        }
	//
	//        try {
	//            Authentication authentication = jwtProvider.getUserDetails(userIdentifier);
	//            SecurityContextHolder.getContext().setAuthentication(authentication);
	//
	//            System.out.println("인증 성공 : " + userIdentifier);
	//        } catch (Exception e) {
	//            System.out.println("JWT 검증 실패: " + e.getMessage());
	//            sendUnauthorizedResponse(response, "유효하지 않은 JWT 토큰");
	//            return;
	//        }
	//
	//        System.out.println("[JWTFilter 마지막] SecurityContextHolder 인증 객체: " +
	//                SecurityContextHolder.getContext().getAuthentication());
	//        filterChain.doFilter(request, response);
	//    }

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
		cookie.setDomain("toleave.shop");
		return cookie;
	}

	// 인증 실패 응답 보내기
	private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		CommonResponse<String> errorResponse = CommonResponse.error(401, message);

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResponse = objectMapper.writeValueAsString(errorResponse);

		response.getWriter().write(jsonResponse);
	}

}
