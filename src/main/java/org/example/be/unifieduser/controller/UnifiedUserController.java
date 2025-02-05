package org.example.be.unifieduser.controller;


import lombok.RequiredArgsConstructor;
import org.example.be.response.CommonResponse;
import org.example.be.unifieduser.dto.*;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UnifiedUserController {

    private final UnifiedUserService unifiedUserService;

    // 통합 유저 삭제 ( 회원 탈퇴 )
    @DeleteMapping("/{userIdentifier}")
    public ResponseEntity<CommonResponse<String>> deleteUser(@PathVariable String userIdentifier) {
        unifiedUserService.deleteUnifiedUser(userIdentifier);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "회원 탈퇴 성공"));
    }

    // 이용약관 동의여부 변경
    @PostMapping("/change/policy")
    public ResponseEntity<CommonResponse<String>> changePolicy(@RequestBody PolicyUpdateRequestDTO request) {
        unifiedUserService.updatePolicy(request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "이용약관 동의여부 변경 성공"));
    }


    // 유료구독 이용여부 변경
    @PostMapping("/change/subscribed")
    public ResponseEntity<CommonResponse<String>> changeSubscribed(@RequestBody SubscribedUpdateRequestDTO request) {
        unifiedUserService.updateSubscribed(request);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "유료구독 여부 변경 성공"));
    }

/*    // 회원이름 변경 - 간단하게만 만들어뒀어용 머지 시 참고예정입니당
    @PostMapping("/modify/username")
    public ResponseEntity<CommonResponse<String>> modifyUserName(@RequestBody ModifyNameDTO request) {
        unifiedUserService.updateName(request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "이름 변경 성공"));
    }*/
    // 유저 프로필 조회
    @GetMapping("/{userIdentifier}/profile")
    public ResponseEntity<CommonResponse<UnifiedUserProfileDTO>> getUserProfile(@PathVariable String userIdentifier) {
        UnifiedUserProfileDTO profileDTO = unifiedUserService.getUserProfile(userIdentifier);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(profileDTO, "유저 프로필 조회 성공"));
    }

    // 프로필 사진 수정
    @PostMapping("/{userIdentifier}/profile-picture")
    public ResponseEntity<CommonResponse<String>> updateProfilePicture(
            @PathVariable String userIdentifier,
            @RequestBody ProfilePictureDTO request) {
        unifiedUserService.updateProfilePicture(userIdentifier, request.getNewProfilePictureUrl());
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "프로필 사진 수정 성공"));
    }

    // 이름 수정
    @PostMapping("/{userIdentifier}/name")
    public ResponseEntity<CommonResponse<String>> updateName(
            @PathVariable String userIdentifier,
            @RequestBody ModifyNameDTO request) {
        unifiedUserService.updateName(userIdentifier, request.getName());
        return ResponseEntity.ok(CommonResponse.success(null, "이름 수정 성공"));
    }

    // 이메일 수정 (일반 유저만)
    @PreAuthorize("!@unifiedUserService.isSocialUser(authentication.name)") // 소셜 로그인 유저 이메일 수정 제한
    @PostMapping("/{userIdentifier}/email")
    public ResponseEntity<CommonResponse<String>> updateEmail(
            @PathVariable String userIdentifier,
            @RequestBody ModifyEmailDTO request) {
        unifiedUserService.updateEmail(userIdentifier, request.getNewEmail());
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "이메일 수정 성공"));
    }

    // 비밀번호 수정 (일반 로그인 사용자)
    @PreAuthorize("!@unifiedUserService.isSocialUser(authentication.name)") // #userIdentifier는 PathVariable로 받아온 유저 식별자
    @PostMapping("/{userIdentifier}/password")
    public ResponseEntity<CommonResponse<String>> updatePassword(
            @PathVariable String userIdentifier,
            @RequestBody ModifyPasswordDTO request) {
        unifiedUserService.updatePassword(userIdentifier, request.getNewPassword());
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "비밀번호 수정 성공"));
    }

    // 소셜 계정 연동
    @PostMapping("/{userIdentifier}/link-social")
    public ResponseEntity<CommonResponse<String>> linkSocialAccount(
            @PathVariable String userIdentifier,
            @RequestBody LinkSocialDTO request) {
        unifiedUserService.linkSocialAccount(userIdentifier, request.getSocialProvider(), request.getSocialIdentifier());
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "소셜 계정 연동 성공"));
    }
}