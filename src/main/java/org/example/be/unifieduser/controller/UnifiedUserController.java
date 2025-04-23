package org.example.be.unifieduser.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.response.CommonResponse;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.dto.*;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    public ResponseEntity<CommonResponse<String>> changePolicy(@RequestBody SubscribedUpdateRequestDTO request) {
        unifiedUserService.updateSubscribed(request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "유료구독 여부 변경 성공"));
    }

    // 회원이름 변경 - 간단하게만 만들어뒀어용 머지 시 참고예정입니당
    @PostMapping("/modify/username")
    public ResponseEntity<CommonResponse<String>> modifyUserName(@RequestBody ModifyNameDTO request) {
        unifiedUserService.updateName(request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "이름 변경 성공"));
    }

    // 유저 정보 조회 ( 프로필 이라 칭하겠음 )
    // !주의 : 요청자의 정보 조회가 이루어지는 게 아님. 조회하고자 하는 유저 userIdentifier 입력으로 정보 조회.
    @GetMapping("/getProfile/{userIdentifier}")
    public ResponseEntity<CommonResponse<UnifiedUserDTO>> getUserProfile(@PathVariable String userIdentifier) {
        UnifiedUserDTO userDTO = unifiedUserService.getUserProfile(userIdentifier);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(userDTO, "프로필 조회 성공"));
    }

    // 프로필 사진 변경
    @PostMapping("/change/profileImage")
    public ResponseEntity<CommonResponse<String>> updateProfileImage(@RequestParam MultipartFile file) throws IOException {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        String profileImageUrl = unifiedUserService.updateProfileImageUrl(userIdentifier, file);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(profileImageUrl, "프로필 사진 변경 성공"));
    }

    // 프로필 사진 삭제
    @DeleteMapping("/profileImage/delete")
    public ResponseEntity<CommonResponse<Void>> deleteProfileImage() {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        unifiedUserService.deleteProfileImage(userIdentifier);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "프로필 사진 삭제 성공"));
    }

    // 해당 유저 이름, 프로필사진만 가져오기 ( 그룹 채팅에서 사용 예정 )
    @GetMapping("/getNameAndImage/{userIdentifier}")
    public ResponseEntity<CommonResponse<UnifiedUsersNameAndProfileImageUrl>> getNameAndProfileImageUrl(@PathVariable String userIdentifier) {
        UnifiedUsersNameAndProfileImageUrl nameAndProfileImageUrl = unifiedUserService.getNameAndProfileImageUrlByUserIdentifier(userIdentifier);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(nameAndProfileImageUrl, "이름과 프로필 사진 가져오기 성공"));
    }

}
