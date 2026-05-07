package org.example.be.global.security.oauth.service;

import java.util.Map;

import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.domain.member.type.OauthProvider;
import org.example.be.global.security.oauth.userinfo.GoogleUserInfo;
import org.example.be.global.security.oauth.userinfo.KakaoUserInfo;
import org.example.be.global.security.oauth.userinfo.NaverUserInfo;
import org.example.be.global.security.oauth.userinfo.OAuth2UserInfo;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service // 이 클래스는 Spring의 서비스 빈으로 등록되어 DI(의존성 주입) 받는다.
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	private final MemberRepository memberRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest)
		throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);

		String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

		OauthProvider provider = OauthProvider.valueOf(providerTypeCode);

		OAuth2UserInfo userInfo = getOAuth2UserInfo(providerTypeCode, oAuth2User.getAttributes());

		// 이메일 단독 UNIQUE 정책에 맞춰 email만으로 조회, 이후 member 객체 필드의 provider를 비교
		Member member = memberRepository.findByEmail(userInfo.getEmail()).orElse(null);

		if (member == null) {
			// 신규 가입인 경우
			joinMember(userInfo, provider);
		} else if (member.getOauthProvider() != provider) {
			// 동일 이메일이 다른 provider (또는 General)로 이미 가입되어 있는 경우
			// 개인정보 보호를 위해 어떤 provider로 가입되었는지는 노출하지 않음
			throw new OAuth2AuthenticationException(new OAuth2Error("email_already_registered"),
				"다른 로그인 방식으로 가입된 이메일입니다. 해당 방식으로 로그인해주세요.");
		}

		// email 존재, 같은 provider면 그대로 통과 -> SuccessHandler에서 토큰 발급
		return oAuth2User;
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

	private void joinMember(OAuth2UserInfo userInfo, OauthProvider provider) {
		memberRepository.save(
			Member.createForOAuth(userInfo.getName(), userInfo.getEmail(), userInfo.getProfileImage(), provider));
	}
}
