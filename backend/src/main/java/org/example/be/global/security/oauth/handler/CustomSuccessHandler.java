package org.example.be.global.security.oauth.handler;

import java.io.IOException;
import java.util.Map;

import org.example.be.domain.group.invitation.entity.GroupInvitation;
import org.example.be.domain.group.invitation.service.GroupInvitationService;
import org.example.be.domain.group.invitation.util.InviteRedirectHelper;
import org.example.be.domain.group.service.GroupService;
import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.domain.member.service.AuthTokenService;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.security.oauth.userinfo.GoogleUserInfo;
import org.example.be.global.security.oauth.userinfo.KakaoUserInfo;
import org.example.be.global.security.oauth.userinfo.NaverUserInfo;
import org.example.be.global.security.oauth.userinfo.OAuth2UserInfo;
import org.example.be.global.util.CookieHelper;
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
	private final InviteRedirectHelper inviteRedirectHelper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		OAuth2AuthenticationToken token = (OAuth2AuthenticationToken)authentication;
		String providerTypeCode = token.getAuthorizedClientRegistrationId().toUpperCase();

		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		OAuth2UserInfo userInfo = getOAuth2UserInfo(providerTypeCode, oAuth2User.getAttributes());

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

		// 초대 쿠키가 없는 일반 로그인은 홈으로 보낸다.
		String redirectUrl = inviteRedirectHelper.homeUrl();

		// pendingInvitationCode 쿠키 확인 (비로그인 상태에서 초대링크 클릭 후 소셜로그인한 경우)
		String invitationCode = extractPendingInvitationCode(request);
		if (invitationCode != null) {
			try {
				redirectUrl = joinGroupAndResolveRedirectUrl(invitationCode, member.getId());
			} finally {
				// 가입 성공/실패 여부 무관하게 쿠키 즉시 삭제
				deletePendingInvitationCodeCookie(response);
			}
		}
		response.sendRedirect(redirectUrl);
	}

	// 초대 코드로 그룹 가입을 시도하고 그 결과에 맞는 리다이렉트 URL을 돌려준다.
	// 로그인 자체는 이미 성공한 상태이므로 초대 처리 실패가 로그인을 깨서는 안된다.
	private String joinGroupAndResolveRedirectUrl(String invitationCode, Long memberId) {
		Long groupId = null;

		try {
			GroupInvitation invitation = groupInvitationService.getValidInvitation(invitationCode);
			// GroupInvitation.group은 @ManyToOne(기본 EAGER)이라 트랜잭션 밖인 이 필터 계층에서도 접근이 안전하다.
			// 이후 LAZY로 변경되면 이 지점이 깨지므로 주의할 것.
			groupId = invitation.getGroup().getId();

			groupService.addMemberToGroup(invitation.getGroup().getGroupName(), memberId);
			return inviteRedirectHelper.groupPageUrl(groupId, InviteRedirectHelper.JOINED_NEW);
		} catch (BusinessException e) {
			// 이미 멤버인 경우는 실패가 아니라 성공으로 취급한다. (InvitationJoinController와 동일한 정책)
			if (e.getErrorCode() == ErrorCode.GROUP_ALREADY_MEMBER && groupId != null) {
				log.info("[invite] 소셜 로그인 후 이미 그룹 멤버 - memberId={}, groupId={}", memberId, groupId);
				return inviteRedirectHelper.groupPageUrl(groupId, InviteRedirectHelper.JOINED_ALREADY);
			}

			log.warn("[invite] 소셜 로그인 후 그룹 자동 가입 실패 - memberId={}, errorCode={}, message={}",
				memberId, e.getErrorCode(), e.getMessage());
			return inviteRedirectHelper.inviteErrorUrl(e.getErrorCode().name());
		} catch (Exception e) {
			log.error("[invite] 소셜 로그인 후 그룹 자동 가입 중 예기지 못한 오류 - memberId={}", memberId, e);
			return inviteRedirectHelper.inviteErrorUrl(ErrorCode.INTERNAL_SERVER_ERROR.name());
		}
	}

	private String extractPendingInvitationCode(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : request.getCookies()) {
			if ("pendingInvitationCode".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private void deletePendingInvitationCodeCookie(HttpServletResponse response) {
		ResponseCookie deleteCookie = ResponseCookie.from("pendingInvitationCode", "")
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(0)    // 쿠키 즉시 삭제
			.sameSite("Lax")
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
	}

	private OAuth2UserInfo getOAuth2UserInfo(String providerTypeCode, Map<String, Object> attributes) {
		if ("KAKAO".equalsIgnoreCase(providerTypeCode)) {
			return new KakaoUserInfo(attributes);
		} else if ("NAVER".equalsIgnoreCase(providerTypeCode)) {
			return new NaverUserInfo(attributes);
		} else if ("GOOGLE".equalsIgnoreCase(providerTypeCode)) {
			return new GoogleUserInfo(attributes);
		}

		throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다: " + providerTypeCode);
	}

}