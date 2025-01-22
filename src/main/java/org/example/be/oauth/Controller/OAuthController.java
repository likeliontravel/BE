package org.example.be.oauth.Controller;

import org.example.be.oauth.dto.CustomOAuth2User;
import org.example.be.oauth.dto.SocialUserDTO;
import org.example.be.oauth.service.SocialUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    private final SocialUserService socialUserService;

    public OAuthController(SocialUserService socialUserService) {
        this.socialUserService = socialUserService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        // 인증된 사용자 정보 가져오기
        if (customOAuth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // name과 email을 포함한 사용자 정보 반환
        SocialUserDTO socialUserDTO = socialUserService.getUserProfile(customOAuth2User.getUsername()); // username으로 사용자 정보 조회
        return ResponseEntity.ok(socialUserDTO); // SocialUserDTO 반환
    }
}