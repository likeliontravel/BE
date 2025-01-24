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

        // 소셜 유저 조회
        SocialUser socialUser = socialUserRepository.findByUserIdentifier(userIdentifier).orElse(null);

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

            // 통합 유저 생성
            unifiedUserService.createUnifiedUser(
                    new UnifiedUserCreationRequestDTO(provider, email, name, "ROLE_USER")
            );
        } else {    // 기존 회원 데이터가 있을 경우 회원정보 업데이트.. 가 문제가 아니라 형님 생각해보니깐 아주 중대한 문제가 있는데요..?
                    // 생각해보니 애초에 통합 테이블에서 소셜 테이블로의 조회는 괜찮은데,
                    // 시큐리티에서 소셜테이블로 조회를 할 때 식별자로 email, userIdentifier 둘 다 사용하면 안되는 문제가 있네요...?
                    // 이거 그냥 Provider에서 제공해주는 providerId로 변경해야할 것 같아요
                    // 빠르게 이거 수정해놓을게요 다른 작업 진행하셔도 됩니다 다른 로직 수행하는데 영향이 가진 않아요
            socialUser.setName(name);
            socialUser.setProvider(provider);
            socialUser.setEmail(email);
            socialUser.setUserIdentifier(userIdentifier);
            socialUserRepository.save(socialUser);
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
