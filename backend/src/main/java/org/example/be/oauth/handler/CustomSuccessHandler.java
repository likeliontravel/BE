package org.example.be.oauth.handler;

import java.io.IOException;
import java.util.Map;

import org.example.be.group.dto.GroupAddMemberRequestDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
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

		// 초대 코드가 있을 시 자동 그룹 가입 처리
		String invitationCode = request.getParameter("invitationCode");
		if (invitationCode != null && !invitationCode.isEmpty()) {
			try {
				var invitation = groupInvitationService.getValidInvitation(invitationCode);
				GroupAddMemberRequestDTO dto = new GroupAddMemberRequestDTO();
				dto.setGroupName(invitation.getGroup().getGroupName());
				dto.setUserIdentifier(member.getName());
				groupService.addMemberToGroup(dto);
			} catch (Exception e) {
				// 초대 코드가 유효하지 않아도 로그인 자체는 성공이므로, 여기서 전체 플로우를 깨지 않도록 예외 메시지 로깅만 해준다.
				System.out.println("OAuth2 로그인 후 자동 그룹 가입 실패 : " + e.getMessage());
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