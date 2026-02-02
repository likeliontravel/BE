package org.example.be.oauth.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.example.be.group.dto.GroupAddMemberRequestDTO;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.example.be.member.service.AuthTokenService;
import org.example.be.oauth.dto.KakaoResponse;
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

		String redirectUrl = "http://localhost:3000/main";
		String stateParam = request.getParameter("state");
		if (stateParam != null) {
			String decodedState = new String(Base64.getUrlDecoder().decode(stateParam), StandardCharsets.UTF_8);
			redirectUrl = decodedState.split("#", 2)[0];

			// 최종 리다이렉트
			response.sendRedirect(redirectUrl);

			// ResponseCookie로 전환하기 이전 코드. 테스트 이후 문제 없을 시 삭제 예정
			//        //OAuth2User
			//        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
			//        String userIdentifier = customUserDetails.getUserIdentifier();
			//        String role = customUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_USER");
			//
			//        // AccessToken, RefreshToken 발급
			//        String accessToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60); //1시간(1000L * 60 * 60), 2분(1000L * 60 * 2)
			//        String refreshToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60 * 24 * 7); // 7일(1000L * 60 * 60 * 24 * 7)
			//
			//        System.out.println("로그인 성공: " + userIdentifier);
			//        System.out.println("생성된 accessToken 토큰 : " + accessToken);
			//        System.out.println("생성된 refreshToken 토큰 : " + refreshToken);
			//
			//        // 쿠키, 헤더 각각 추가
			//        response.addCookie(createCookie("Authorization", accessToken));
			//        response.addCookie(createCookie("Refresh-Token", refreshToken));
			//        response.setHeader("Authorization", "Bearer " + accessToken);
			//        response.setHeader("Refresh-Token", "Bearer " + refreshToken);
			//
			//        // OAuth2 로그인 성공 후 URL 쿼리 파라미터에서  invitationCode가 있는지 확인
			//        String invitationCode = request.getParameter("invitationCode");
			//        if (invitationCode != null && !invitationCode.isEmpty()) {  // invitationCode가 파라미터에 포함되어 있었다면 자동으로 해당 그룹에 멤버 추가
			//            try {
			//                var invitation = groupInvitationService.getValidInvitation(invitationCode);
			//                GroupAddMemberRequestDTO dto = new GroupAddMemberRequestDTO();
			//                dto.setGroupName(invitation.getGroup().getGroupName());
			//                dto.setUserIdentifier(userIdentifier);
			//                groupService.addMemberToGroup(dto);
			//            } catch (Exception e) {
			//                System.out.println("OAuth2 로그인 후 자동 그룹 가입 실패 : " + e.getMessage());
			//                throw new IllegalArgumentException("초대 코드가 유효하지 않습니다. " + e.getMessage());
			//            }
			//        }
			//
			//        // SecurityContext에 인증 정보 저장하기
			//        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
			//        context.setAuthentication(authentication);
			//        SecurityContextHolder.setContext(context);
			//
			//        response.sendRedirect("https://toleave.shop/");
		}

		// JWTUtil.createCookie 사용으로 통일하기로 결정. 문제 생길 시 복구 예정
		//    private Cookie createCookie(String key, String value) {
		//        Cookie cookie = new Cookie(key, value);
		//        cookie.setMaxAge(60 * 60 * 24 * 7);
		//        cookie.setSecure(true); // https 적용 시 주석 해제할 것
		//        cookie.setPath("/");
		//        cookie.setHttpOnly(true);
		//
		//        //System.out.println("Cookie: " + cookie.getValue());
		//
		//        return cookie;
		//    }

	}

	private OAuth2Response getOAuth2UserInfo(String providerTypeCode, Map<String, Object> attributes) {
		if ("KAKAO".equalsIgnoreCase(providerTypeCode)) {
			return new KakaoResponse(attributes);
		}
		throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다: " + providerTypeCode);
	}

}