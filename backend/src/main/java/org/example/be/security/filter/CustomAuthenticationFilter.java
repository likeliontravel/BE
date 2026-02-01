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
		FilterChain filterChain) throws
		ServletException,
		IOException {
		try {
			// Preflight(OPTIONS)은 그대로 통과
			if (HttpMethod.OPTIONS.matches(request.getMethod())) {
				filterChain.doFilter(request, response);
				return;
			}

			// Access Token 검사 (헤더 → 쿠키)
			String accessToken = resolveAccessToken();
			if (!accessToken.isBlank()) {
				Map<String, Object> claims = authTokenService.payload(accessToken);
				if (claims != null) {
					Long memberId = Long.valueOf(String.valueOf(claims.get("id")));
					Member member = memberService.getById(memberId);
					if (member != null) {
						setAuthenticationFromUser(member);
						filterChain.doFilter(request, response);
					}
					return;
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

	/** DB(User) 객체 기반 인증 세팅 */
	private void setAuthenticationFromUser(Member member) {
		var authorities = member.getAuthorities();
		SecurityUser principal = new SecurityUser(
			member.getId(), member.getEmail(), "", member.getName(), authorities
		);
		Authentication auth = new UsernamePasswordAuthenticationToken(principal, "", authorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private String resolveAccessToken() {
		return cookieHelper.getCookieValue("accessToken", "");
	}

}