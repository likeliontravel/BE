package org.example.be.global.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.service.AuthTokenService;
import org.example.be.domain.member.service.MemberService;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.support.ErrorResponseWriter;
import org.example.be.global.security.config.SecurityUser;
import org.example.be.global.util.CookieHelper;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFilter extends OncePerRequestFilter {
	private final MemberService memberService;
	private final CookieHelper cookieHelper;
	private final AuthTokenService authTokenService;
	private final ErrorResponseWriter errorResponseWriter;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		// Preflight(OPTIONS)은 그대로 통과
		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			authenticate(request);    // 인증 세팅만 담당, 응답을 여기서 쓰지 않는 것으로 변경
		} catch (BusinessException e) {
			// 필터는 @RestControllerAdvice에 도달하지 못하므로 여기서 직접 응답 규격을 쓴다.
			// 현재 로직에서는 authenticate()가 예외를 삼키므로 거의 발동하지 않지만,
			// "필터에서 난 도메인 예외는 CommonResponse 규격 JSON으로 나간다"는 걸 코드에 드러내기 위해 남긴다.
			// 이 자리가 비어 있으면 다음 사람이 response.setStatus()를 다시 쓰게 된다.
			log.warn("[CustomAuthenticationFilter] 인증 실패 - code={}, detail={}",
				e.getErrorCode().name(), e.getMessage());
			errorResponseWriter.write(response, e.getErrorCode());
			return;
		}

		// doFilter는 try 밖이다.
		// 안에 두면 이 필터 하위(컨트롤러 advice, 다른 필터)에서 난 예외까지 잡아서 401로 뭉갠다.
		filterChain.doFilter(request, response);
	}

	/**
	 * 인증 컨텍스트만 세팅한다. 성공/실패와 무관하게 응답을 건드리지 않는다.
	 *
	 * 토큰이 없거나 만료된 것은 '오류'가 아니라 '익명'이다 - permitAll 경로가 동작해야 하므로
	 * 익명 인증을 세팅하고 정상 반환한다. 인증이 필요한 경로는 뒤의 entryPoint가 401을 만든다.
	 */
	private void authenticate(HttpServletRequest request) {
		// 1) AccessToken 검사 (헤더 -> 쿠키)
		String accessToken = resolveToken(request);
		if (!accessToken.isBlank()) {
			Map<String, Object> claims = authTokenService.payload(accessToken);
			if (claims != null) {
				setAuthenticationFromClaims(claims);
				return;
			}
		}

		// 2) Refresh Token으로 재발급
		String refreshToken = cookieHelper.getCookieValue("refreshToken", "");
		if (refreshToken != null && !refreshToken.isBlank()) {
			try {
				long userId = authTokenService.findRefreshOwner(refreshToken);
				Member member = memberService.getById(userId);
				String newRefreshToken = authTokenService.rotateRefresh(refreshToken);
				String newAccessToken = authTokenService.genAccessToken(member);

				// 응답 헤더와 쿠키에 새 토큰 세팅
				cookieHelper.setCookie("refreshToken", newRefreshToken);
				cookieHelper.setCookie("accessToken", newAccessToken);

				setAuthenticationFromUser(member);
				return;
			} catch (BusinessException e) {
				// catch를 BusinessException으로 좁혔다.
				// 만료 또는 손상된 refresh 토큰은 '정상적인 실패'이므로 쿠키를 정리하고 익명으로 계속 진행한다.
				// Redis 연결 실패, payload파싱 오류 (IllegalArgumentException)는 여기서 잡히지 않고
				// 그대로 전파되어 정직하게 500이 된다. 기존 catch (Exception)은 이 둘을 구분하지 못했다.

				// stack trace를 남기지 않는 이유: 이 catch에 도달할 수 있는 BusinessException은
				// AuthTokenService 4곳 + MemberService.getById 1곳 뿐이고 전부 cause가 없다.
				// 즉 스택이 code와 jti 이상을 알려주지 않는다. 원인 추적이 필요한 진짜 장애는
				// 위 설명대로 전파되어 catch-all이 ERROR + 전체 스택으로 남긴다.
				log.warn("[CustomAuthenticationFilter] Refresh token 갱신 실패 - code={}, detail={}",
					e.getErrorCode().name(), e.getMessage());

				cookieHelper.deleteCookie("refreshToken");
				cookieHelper.deleteCookie("accessToken");
			}
		}

		// 3) 토큰이 없거나 갱신에 실패했다 - 익명 인증 설정 (permitAll 경로를 위해)
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			AnonymousAuthenticationToken anonymousAuth = new AnonymousAuthenticationToken(
				"anonymous",
				"anonymousUser",
				List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
			);
			SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
		}
	}

	private void setAuthenticationFromClaims(Map<String, Object> claims) {
		long memberId = ((Number)claims.get("id")).longValue();
		String email = (String)claims.get("email");
		String name = (String)claims.get("name");
		String role = (String)claims.get("role");

		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
		SecurityUser principal = new SecurityUser(
			memberId, email, "", name, List.of(authority)
		);
		Authentication auth = new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

	}

	/** DB(User) 객체 기반 인증 세팅 */
	private void setAuthenticationFromUser(Member member) {
		var authorities = member.getAuthorities();
		SecurityUser principal = new SecurityUser(
			member.getId(), member.getEmail(), "", member.getName(), authorities
		);
		Authentication auth = new UsernamePasswordAuthenticationToken(principal, "", authorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	// 포스트맨에서 Authorization 헤더에 Bearer 토큰을 넣어 테스트할 수 있는 메서드
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		return cookieHelper.getCookieValue("accessToken", "");
	}

}