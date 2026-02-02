package org.example.be.oauth.service;

import java.util.Map;

import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.example.be.member.type.OauthProvider;
import org.example.be.oauth.dto.KakaoResponse;
import org.example.be.oauth.dto.OAuth2Response;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service // 이 클래스는 Spring의 서비스 빈으로 등록되어 DI(의존성 주입) 받는다.
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest)
		throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);

		String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

		OauthProvider provider = OauthProvider.valueOf(providerTypeCode);

		OAuth2Response userInfo = getOAuth2UserInfo(providerTypeCode, oAuth2User.getAttributes());

		Member member = memberRepository.findByProviderAndEmail(provider, userInfo.getEmail()).orElse(null);

		if (member == null) {
			joinMember(userInfo, provider);
		}

		return oAuth2User;
	}

	private OAuth2Response getOAuth2UserInfo(String providerTypeCode, Map<String, Object> attributes) {
		if ("KAKAO".equalsIgnoreCase(providerTypeCode)) {
			return new KakaoResponse(attributes);
		}
		throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다: " + providerTypeCode);
	}

	private void joinMember(OAuth2Response userInfo, OauthProvider provider) {
		memberRepository.save(
			Member.createForOAuth(userInfo.getName(), userInfo.getEmail(), userInfo.getProfileImage(), provider));
	}
}
