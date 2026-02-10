package org.example.be.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.example.be.member.entity.Member;
import org.example.be.member.service.AuthTokenService;
import org.example.be.member.service.MemberService;
import org.example.be.security.config.SecurityUser;
import org.example.be.web.CookieHelper;
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

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
	private final MemberService memberService;
	private final CookieHelper cookieHelper;
	private final AuthTokenService authTokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			// Preflight(OPTIONS)은 그대로 통과
			if (HttpMethod.OPTIONS.matches(request.getMethod())) {
				filterChain.doFilter(request, response);
				return;
			}

			// Access Token 검사 (헤더 → 쿠키)
			String accessToken = resolveToken(request);
			if (!accessToken.isBlank()) {
				Map<String, Object> claims = authTokenService.payload(accessToken);
				if (claims != null) {
					setAuthenticationFromClaims(claims);
					filterChain.doFilter(request, response);
					return;
				}
			}

			String getRefreshToken = cookieHelper.getCookieValue("refreshToken", "");
			if (getRefreshToken != null && !getRefreshToken.isBlank()) {
				try {
					long userId = authTokenService.findRefreshOwner(getRefreshToken);
					String newRefreshToken = authTokenService.rotateRefresh(getRefreshToken);
					Member member = memberService.getById(userId);
					String newAccessToken = authTokenService.genAccessToken(member);

					// 응답 헤더와 쿠키에 새 토큰 세팅
					cookieHelper.setCookie("refreshToken", newRefreshToken);
					response.setHeader("Authorization", newAccessToken);

					setAuthenticationFromUser(member);

					filterChain.doFilter(request, response);
					return;

				} catch (Exception e) {
					cookieHelper.deleteCookie("refreshToken");
					cookieHelper.deleteCookie("accessToken");
				}
			}

			// ✅ 토큰이 없으면 익명 인증 설정 (permitAll 경로를 위해)
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				AnonymousAuthenticationToken anonymousAuth = new AnonymousAuthenticationToken(
					"anonymous",
					"anonymousUser",
					List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
				);
				SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
			}

			filterChain.doFilter(request, response);

		} catch (RuntimeException e) {
			// 서비스 레벨 예외는 상태코드만 세팅하고 종료
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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