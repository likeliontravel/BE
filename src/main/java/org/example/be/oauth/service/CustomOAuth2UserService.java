package org.example.be.oauth.service;

import lombok.RequiredArgsConstructor;
import org.example.be.oauth.dto.*;
import org.example.be.oauth.entity.SocialUser;
import org.example.be.oauth.repository.SocialUserRepository;
import org.example.be.unifieduser.dto.UnifiedUserCreationRequestDTO;
import org.example.be.unifieduser.dto.UnifiedUserDTO;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service // 이 클래스는 Spring의 서비스 빈으로 등록되어 DI(의존성 주입) 받는다.
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialUserRepository socialUserRepository;
    private final UnifiedUserRepository unifiedUserRepository;
    private final UnifiedUserService unifiedUserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        System.out.println("OAuth2 사용자 정보 로드 완료: " + attributes);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();    // provider

        OAuth2Response response = createOAuth2Response(oAuth2User.getAttributes(), registrationId);
        OAuth2User customOAuth2User = processOAuth2User(response, attributes);

        setAuthenticationContext(customOAuth2User);
        return customOAuth2User;
    }

    // 각 Provider에 맞는 ResponseDTO 생성
    private OAuth2Response createOAuth2Response(Map<String, Object> attributes, String provider) {
        switch (provider) {
            case "google":
                return new GoogleResponse(attributes);
            case "kakao":
                return new KakaoResponse(attributes);
            case "naver":
                return new NaverResponse(attributes);
            default:
                throw new IllegalArgumentException("지원되지 않는 provider입니다. : " + provider);
        }
    }

    // SocialUser테이블에 저장
    private OAuth2User processOAuth2User(OAuth2Response response, Map<String, Object> attributes) {
        String email = response.getEmail();
        String name = response.getName();
        String provider = response.getProvider();
        String providerId = response.getProviderId();
        String userIdentifier = provider + "_" + email;

        System.out.println("생성된 userIdentifier: " + userIdentifier);

        // 소셜 유저 조회 (ProviderId 기반)
        Optional<SocialUser> existingSocialUser = socialUserRepository.findByProviderId(providerId);
        Optional<UnifiedUser> existingUnifiedUser = unifiedUserRepository.findByUserIdentifier(userIdentifier);

        SocialUser socialUser;

        if (existingSocialUser.isEmpty()) {
            socialUser = new SocialUser();
            socialUser.setEmail(email);
            socialUser.setName(name);
            socialUser.setProvider(provider);
            socialUser.setProviderId(providerId);
            socialUser.setRole("ROLE_USER");
            socialUser.setUserIdentifier(userIdentifier);

            socialUserRepository.save(socialUser);

            // UnifiedUser가 없으면 생성
            if (existingUnifiedUser.isEmpty()) {
                unifiedUserService.createUnifiedUser(
                        new UnifiedUserCreationRequestDTO(provider, email, name, "ROLE_USER")
                );
            }
        } else {
            socialUser = existingSocialUser.get();
        }

        return new CustomOAuth2User(socialUser, attributes);
        // SocialUser 테이블에 없다면 새로 생성

    }

    // SecurityContext에 OAuth2 인증 정보 설정
    private void setAuthenticationContext(OAuth2User customOAuth2User) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);

        System.out.println("SecurityContext에 OAuth2 사용자 정보 저장 완료");
    }

    // 엔티티 -> DTO 변환
    private SocialUserDTO mapToDTO(SocialUser entity) {
        SocialUserDTO dto = new SocialUserDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setProvider(entity.getProvider());
        dto.setUserIdentifier(entity.getUserIdentifier());
        dto.setRole(entity.getRole());
        return dto;
    }
}
