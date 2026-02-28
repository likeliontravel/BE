package org.example.be.oauth.handler;

import java.io.IOException;
import java.util.Map;

import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.example.be.member.service.AuthTokenService;
import org.example.be.oauth.dto.GoogleResponse;
import org.example.be.oauth.dto.KakaoResponse;
import org.example.be.oauth.dto.NaverResponse;
import org.example.be.oauth.dto.OAuth2Response;
import org.example.be.web.CookieHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
	private final MemberRepository memberRepository;
	private final AuthTokenService authTokenService;
	private final CookieHelper cookieHelper;
	private final GroupInvitationService groupInvitationService;
	private final GroupService groupService;

	private static final String FE_REDIRECT_URL = "/";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		OAuth2AuthenticationToken token = (OAuth2AuthenticationToken)authentication;
		String providerTypeCode = token.getAuthorizedClientRegistrationId().toUpperCase();

		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		OAuth2Response userInfo = getOAuth2UserInfo(providerTypeCode, oAuth2User.getAttributes());

		log.info("OAuth2 로그인 성공: provider={}, email={}, name={}",
			userInfo.getProvider(), userInfo.getEmail(), userInfo.getName());

		// DB에서 Member 조회 (CustomOAuth2UserService 에서 이미 join 해뒀다는 가정)
		Member member = memberRepository.findByEmail(userInfo.getEmail())
			.orElseThrow(() -> new IllegalStateException("OAuth2 Member를 찾을 수 없습니다. email=" + userInfo.getEmail()));

		log.info("OAuth2 로그인 성공: memberId={}, email={}", member.getId(), member.getEmail());
		String accessToken = authTokenService.genAccessToken(member);
		String refreshToken = authTokenService.RefreshToken(member);

		cookieHelper.setCookie("accessToken", accessToken);
		cookieHelper.setCookie("refreshToken", refreshToken);
		log.debug("JWT 토큰 생성 완료 및 쿠키 설정 완료");

		// pendingInvitationCode 쿠키 확인 (비로그인 상태에서 초대링크 클릭 후 소셜 로그인한 경우)
		String invitationCode = null;
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("pendingInvitationCode".equals(cookie.getName())) {
					invitationCode = cookie.getValue();
					break;
				}
			}
		}

		if (invitationCode != null) {
			try {
				GroupInvitation invitation = groupInvitationService.getValidInvitation(invitationCode);
				groupService.addMemberToGroup(invitation.getGroup().getGroupName(), member.getId());
			} catch (Exception e) {
				// 그룹 자동 가입 실패 시 로그인 자체는 유지
				log.warn("OAuth2 로그인 후 자동 그룹 가입 실패: {}", e.getMessage());
			} finally {
				// 가입 성공/실패 여부 무관하게 쿠키 즉시 삭제
				ResponseCookie deleteCookie = ResponseCookie.from("pendingInvitationCode", "")
					.httpOnly(true)
					.secure(true)
					.path("/")
					.maxAge(0)    // 쿠키 즉시 삭제
					.sameSite("Lax")
					.build();
				response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
			}
		}

		// 최종 리다이렉트
		response.sendRedirect(FE_REDIRECT_URL);

	}

	private OAuth2Response getOAuth2UserInfo(String providerTypeCode, Map<String, Object> attributes) {
		if ("KAKAO".equalsIgnoreCase(providerTypeCode)) {
			return new KakaoResponse(attributes);
		} else if ("NAVER".equalsIgnoreCase(providerTypeCode)) {
			return new NaverResponse(attributes);
		} else if ("GOOGLE".equalsIgnoreCase(providerTypeCode)) {
			return new GoogleResponse(attributes);
		}

		throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다: " + providerTypeCode);
	}

}