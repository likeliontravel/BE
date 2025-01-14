package org.example.be.oauth.Controller;

import org.example.be.oauth.dto.SocialUserDTO;
import org.example.be.oauth.service.SocialUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyPageController {

    private final SocialUserService socialUserService;

    public MyPageController(SocialUserService socialUserService) {
        this.socialUserService = socialUserService;
    }

    @GetMapping("/api/mypage")
    public SocialUserDTO getMyPage(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            throw new IllegalStateException("사용자가 인증되지 않았습니다.");
        }

        // OAuth2User에서 사용자 이름을 가져옴
        String name = oAuth2User.getAttribute("name");
        if (name == null) {
            throw new IllegalStateException("사용자 이름 정보를 찾을 수 없습니다.");
        }

        return socialUserService.getUserProfile(name);
    }
}