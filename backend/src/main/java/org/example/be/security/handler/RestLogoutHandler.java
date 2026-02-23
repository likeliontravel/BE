package org.example.be.security.handler;

import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.member.service.AuthTokenService;
import org.example.be.member.service.RefreshTokenStore;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class RestLogoutHandler implements LogoutHandler {

	private final JWTUtil jwtUtil;
	private final JWTBlackListService jwtBlackListService;
	private final AuthTokenService authTokenService;
	private final RefreshTokenStore refreshTokenStore;

	@SneakyThrows
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		// 1. 토큰 추출
		String accessToken = extractTokenFromHeaderAndCookie(request, "Authorization");
		String refreshToken = extractTokenFromHeaderAndCookie(request, "Refresh-Token");

		// 2. 토큰 누락 시: 여기서 응답을 쓰지 말고 에러 메시지만 넘기기
		if (accessToken == null || refreshToken == null) {
			request.setAttribute("logoutError", "AccessToken 또는 RefreshToken을 찾을 수 없습니다.");
			return;
		}

		try {
			// 3. 블랙리스트 등록 (보관 만료시각은 '리프레시 토큰' 만료 기준으로 설정
			String userIdentifier;
			if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
				userIdentifier = oAuth2User.getUserIdentifier();
			} else {
				userIdentifier = jwtUtil.getUserIdentifier(accessToken);
			}

			// 4. 인증 컨텍스트 제거
			SecurityContextHolder.clearContext();

			// 5. 쿠키 제거(각 쿠키에 대해 올바른 객체에 속성 세팅)
			Cookie deleteAccessCookie = new Cookie("Authorization", null);
			deleteAccessCookie.setSecure(true);
			deleteAccessCookie.setHttpOnly(true);
			deleteAccessCookie.setMaxAge(0);
			deleteAccessCookie.setPath("/");
			response.addCookie(deleteAccessCookie);

			Cookie deleteRefreshCookie = new Cookie("Refresh-Token", null);
			deleteRefreshCookie.setSecure(true);
			deleteRefreshCookie.setHttpOnly(true);
			deleteRefreshCookie.setMaxAge(0);
			deleteRefreshCookie.setPath("/");
			response.addCookie(deleteRefreshCookie);

			// 성공 시에는 응답을 여기서 쓰지 않고 SuccessHandler가 최종 응답 작성

		} catch (Exception e) {
			// 6. 실패 시에도 응답은 여기서 쓰지 않음. 예외만 넘기고 SuccessHandler가 에러 응답으로 마무리
			request.setAttribute("logoutError", e.getMessage());
		}

		//        try {
		//            // OAuth2 사용자 확인
		//            if(authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User) {
		//                CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
		//                String userKey = oAuth2User.getUserIdentifier();    // OAuth2 사용자 이메일 (provider + " " + providerId)
		//
		//                // OAuth2 사용자 토큰을 플랙리스트에 추가
		//                jwtBlackListService.addToBlackList(userKey, accessToken, refreshToken, jwtUtil.getExpiration(accessToken));
		//
		//                System.out.println("OAuth2 사용자 로그아웃: " + userKey);
		//            } else {
		//                // 일반 사용자 토큰을 블랙리스트에 추가
		//                jwtBlackListService.addToBlackList(jwtUtil.getUserIdentifier(accessToken), accessToken, refreshToken, jwtUtil.getExpiration(accessToken));
		//            }
		//
		//            // SecurityContext에서 인증객체 삭제
		//            SecurityContextHolder.clearContext();
		//
		//            // Authorization, Refresh-Token 쿠키 삭제
		//            Cookie deleteAccessCookie = new Cookie("Authorization", null);
		//            deleteAccessCookie.setSecure(true);
		//            deleteAccessCookie.setHttpOnly(true);
		//            deleteAccessCookie.setMaxAge(0);
		//            deleteAccessCookie.setPath("/");
		//            response.addCookie(deleteAccessCookie);
		//
		//            Cookie deleteRefreshCookie = new Cookie("Refresh-Token", null);
		//            deleteAccessCookie.setSecure(true);
		//            deleteAccessCookie.setHttpOnly(true);
		//            deleteAccessCookie.setMaxAge(0);
		//            deleteAccessCookie.setPath("/");
		//            response.addCookie(deleteRefreshCookie);
		//
		//        } catch (Exception e) {
		//
		//            CommonResponse<String> commonResponse = CommonResponse.error(
		//                    HttpStatus.BAD_REQUEST.value(), e.getMessage()
		//            );
		//
		//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		//            response.setContentType(MediaType.APPLICATION_JSON_VALUE+";charset=utf-8");
		//            response.getWriter().write(objectMapper.writeValueAsString(commonResponse));
		//
		//        }
	}

	// 헤더에서 먼저 토큰 탐색, 발견되지 않으면 쿠키에서 탐색
	private String extractTokenFromHeaderAndCookie(HttpServletRequest request, String headerName) {
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
		return token;
	}
}














