package org.example.be.oauth.service;

import lombok.RequiredArgsConstructor;
import org.example.be.oauth.dto.*;
import org.example.be.oauth.entity.SocialUser;
import org.example.be.oauth.repository.SocialUserRepository;
import org.example.be.unifieduser.dto.UnifiedUserCreationRequestDTO;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;

@Service // 이 클래스는 Spring의 서비스 빈으로 등록되어 DI(의존성 주입) 받는다.
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialUserRepository socialUserRepository; // SocialUserRepository 의존성 주입
    private final UnifiedUserService unifiedUserService;

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
        String userIdentifier = provider + " " + email;
//
//        // 소셜 유저 조회
//        SocialUser socialUser = socialUserRepository.findByUserIdentifier(userIdentifier).orElse(null);

        // 소셜로그인하며 받은 인증 객체로부터 ProviderId를 이용해 소셜 테이블 유저 조회. 여기서는 예외처리를 하면 안됨.
        SocialUser socialUser = socialUserRepository.findByProviderId(providerId).orElse(null);

        // social_user 테이블에 기존 정보가 없을 경우(새로운 유저가 소셜로그인)
        if (socialUser == null) {
            // 소셜 유저 생성
            socialUser = new SocialUser();
            socialUser.setEmail(email);
            socialUser.setName(name);
            socialUser.setProvider(provider);
            socialUser.setProviderId(providerId);
            socialUser.setRole("ROLE_USER");
            socialUser.setUserIdentifier(userIdentifier);

            socialUserRepository.save(socialUser);

            // 통합 유저 생성; 최초 생성시 만들어진 이메일과 이름, userIdentifier만 가져오며, 이후 모든 서비스로직은 통합테이블을 통하므로 정보변경은 통합테이블에서만 이루어짐
            unifiedUserService.createUnifiedUser(
                    new UnifiedUserCreationRequestDTO(provider, email, name, "ROLE_USER")
            );
        } else {
            // 기존 정보 변경 비활성화. ProviderId는 변경될 일이 없으니 setter 불필요
//            socialUser.setName(name);
//            socialUser.setProvider(provider);
//            socialUser.setEmail(email);
//            socialUser.setUserIdentifier(userIdentifier);
//            socialUserRepository.save(socialUser);
        }

        return new CustomOAuth2User(mapToDTO(socialUser));
    }

    // 엔티티 -> DTO 매퍼
    private SocialUserDTO mapToDTO(SocialUser entity) {
        SocialUserDTO dto = new SocialUserDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setProvider(entity.getProvider());
        dto.setRole(entity.getRole());
        return dto;
    }
}
