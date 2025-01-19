package org.example.be.unifieduser.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.response.CommonResponse;
import org.example.be.unifieduser.dto.ModifyNameDTO;
import org.example.be.unifieduser.dto.PolicyUpdateRequestDTO;
import org.example.be.unifieduser.dto.SubscribedUpdateRequestDTO;
import org.example.be.unifieduser.dto.UnifiedUserCreationRequestDTO;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UnifiedUserController {

    private final UnifiedUserService unifiedUserService;

//    // 통합 유저 생성 ( 회원 가입 )
//    @PostMapping("/create")
//    public ResponseEntity<CommonResponse<String>> createUnifiedUser(@RequestBody UnifiedUserCreationRequestDTO request) {
//        unifiedUserService.createUnifiedUser(request);
//
//        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "통합 유저 생성 성공"));
//    }

    // 통합 유저 삭제 ( 회원 탈퇴 )


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


}
