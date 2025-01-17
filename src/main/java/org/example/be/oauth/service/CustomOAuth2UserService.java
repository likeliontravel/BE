package org.example.be.oauth.service;

import lombok.RequiredArgsConstructor;
import org.example.be.oauth.dto.*;
import org.example.be.oauth.entity.SocialUserEntity;
import org.example.be.oauth.repository.SocialUserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service // 이 클래스는 Spring의 서비스 빈으로 등록되어 DI(의존성 주입) 받는다.
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialUserRepository socialUserRepository; // SocialUserRepository 의존성 주입
//    private final UnifiedUserService unifiedUserService;    // 통합 후 주석 해제

    // loadUser 메서드 오버라이드: OAuth2UserRequest 객체를 통해 OAuth2User를 로드
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 제공자 별 알맞는 Response 생성
        OAuth2Response response = createOAuth2Response(oAuth2User.getAttributes(), registrationId);

        // 기존 사용자 조회 또는 생성 및 업데이트
        return processOAuth2User(response);


    }


    // 각 Provider에 맞는 ResponseDTO 생성
    private OAuth2Response createOAuth2Response(Map<String, Object> attributes, String provider) {
        switch (provider) {
            case "google" :
                return new GoogleResponse(attributes);
            case "kakao" :
                return new KakaoResponse(attributes);
            case "naver" :
                return new NaverResponse(attributes);
            default:
                throw new IllegalArgumentException("지원되지 않는 provider입니다. : " + provider);
        }
    }

    // SocialUser테이블에 저장
    private OAuth2User processOAuth2User(OAuth2Response response) {
        String email = response.getEmail();
        String name = response.getName();
        String provider = response.getProvider();
        String providerId = response.getProviderId();

        // 소셜 유저 조회
        SocialUserEntity socialUser = socialUserRepository.findByProviderAndProviderId(provider, providerId).orElse(null);

        // social_user 테이블에 기존 정보가 없을 경우(새로운 유저가 소셜로그인)
        if (socialUser == null) {
//            // 통합 유저 생성   // 통합 구현 후 주석 해제
//            UnifiedUser unifiedUser = unifiedUserService.createUnifiedUser(
//                    provider, email, name, "ROLE_USER", false, false
//            );

            // 소셜 유저 생성
            socialUser = new SocialUserEntity();
            socialUser.setEmail(email);
            socialUser.setName(name);
            socialUser.setProvider(provider);
            socialUser.setRole("ROLE_USER");
//            socialUser.setUnifiedUser(unifiedUser);

            socialUserRepository.save(socialUser);
        } else {    // 기존 회원 데이터가 있을 경우 회원정보 업데이트
            socialUser.setName(name);
            socialUser.setProvider(provider);
            socialUserRepository.save(socialUser);
        }

        return new CustomOAuth2User(mapToDTO(socialUser));
    }

    // 엔티티 -> DTO 매퍼
    private SocialUserDTO mapToDTO(SocialUserEntity entity) {
        SocialUserDTO dto = new SocialUserDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setProvider(entity.getProvider());
        dto.setRole(entity.getRole());
        return dto;
    }




}
