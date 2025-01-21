package org.example.be.unifieduser.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.oauth.dto.SocialUserDTO;
import org.example.be.oauth.service.SocialUserService;
import org.example.be.response.CommonResponse;
import org.example.be.unifieduser.dto.MyPageProfileDTO;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final UnifiedUserService unifiedUserService;

    @GetMapping
    public ResponseEntity<CommonResponse<MyPageProfileDTO>> getMyPage(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            throw new IllegalStateException("사용자가 인증되지 않았습니다.");
        }

        // OAuth2User에서 사용자 이메일 추출
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new IllegalStateException("사용자 이메일 정보를 찾을 수 없습니다.");
        }

        // UnifiedUserService에서 사용자 프로필 정보 조회
        MyPageProfileDTO profileDTO = unifiedUserService.getUserProfileByEmail(email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(profileDTO, "마이페이지 정보 조회 성공"));
    }
}