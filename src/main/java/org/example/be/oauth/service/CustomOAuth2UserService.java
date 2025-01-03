package org.example.be.oauth.service;

import org.example.be.oauth.dto.*;
import org.example.be.oauth.entity.UserEntity;
import org.example.be.oauth.repository.UserSocialRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service // 이 클래스는 Spring의 서비스 빈으로 등록되어 DI(의존성 주입) 받는다.
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserSocialRepository userSocialRepository; // UserSocialRepository 의존성 주입

    // 생성자: UserSocialRepository를 받아서 초기화한다.
    public CustomOAuth2UserService(UserSocialRepository userSocialRepository) {
        this.userSocialRepository = userSocialRepository; // 생성자를 통해 UserSocialRepository 초기화
    }

    // loadUser 메서드 오버라이드: OAuth2UserRequest 객체를 통해 OAuth2User를 로드
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // super.loadUser(userRequest) 호출로 기본 OAuth2User 정보를 불러옴
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User); // 로드된 OAuth2User 정보 출력 (디버깅용)

        // OAuth2 인증 제공자에 대한 등록 ID 확인 (구글인지 확인)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response;

        // 구글 로그인일 경우에만 처리
        if (!registrationId.equals("google")) {
           oAuth2Response = new GoogleResponse(oAuth2User.getAttributes()); // 구글이 아닌 경우 null 반환
        } else if (!registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        // OAuth2Response를 GoogleResponse로 초기화
//        OAuth2Response oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());

        // social 로그인에 사용될 username 생성 (provider + providerId 조합)
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        // 이미 해당 username으로 회원이 존재하는지 확인
        UserEntity existData = userSocialRepository.findByUsername(username);

        // 회원이 존재하지 않으면 새로 회원을 생성
        if (existData == null) {

            // 새로운 사용자 엔티티 생성
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(username);
            userEntity.setEmail(oAuth2Response.getEmail()); // 이메일 설정
            userEntity.setName(oAuth2Response.getName()); // 이름 설정
            userEntity.setRole("ROLE_USER"); // 기본 역할을 ROLE_USER로 설정

            // 데이터베이스에 사용자 정보 저장
            userSocialRepository.save(userEntity);

            // SocialUserDTO 객체 생성하여 사용자 정보를 담기
            SocialUserDTO socialUserDTO = new SocialUserDTO();
            socialUserDTO.setUsername(username);
            socialUserDTO.setName(oAuth2Response.getName());
            socialUserDTO.setRole("ROLE_USER"); // 기본 역할 설정

            // CustomOAuth2User 객체를 생성하여 반환
            return new CustomOAuth2User(socialUserDTO);
        }
        // 회원이 이미 존재하는 경우, 정보를 업데이트
        else {

            // 기존 회원 정보 업데이트
            existData.setEmail(oAuth2Response.getEmail());
            existData.setName(oAuth2Response.getName());

            // 업데이트된 사용자 정보 저장
            userSocialRepository.save(existData);

            // SocialUserDTO 객체 생성하여 업데이트된 사용자 정보 담기
            SocialUserDTO socialUserDTO = new SocialUserDTO();
            socialUserDTO.setUsername(existData.getUsername());
            socialUserDTO.setName(oAuth2Response.getName());
            socialUserDTO.setRole(existData.getRole()); // 기존 역할 유지

            // CustomOAuth2User 객체를 생성하여 반환
            return new CustomOAuth2User(socialUserDTO);
        }
    }
}
