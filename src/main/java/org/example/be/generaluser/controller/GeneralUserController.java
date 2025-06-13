package org.example.be.generaluser.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.dto.GeneralUserUpdatePasswordDTO;
import org.example.be.group.dto.GroupAddMemberRequestDTO;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.response.CommonResponse;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.example.be.generaluser.service.GeneralUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/general-user")
public class GeneralUserController {

    private final GeneralUserService generalUserService;
    private final GroupInvitationService groupInvitationService;
    private final GroupService groupService;

    // 회원가입
    // ---- 2025.02.03 변경 ----
    // 회원가입 시 초대 코드가 전달된다면 자동으로 그룹 가입 처리
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<String>> signUp(@RequestBody GeneralUserDTO generalUserDTO
    , @RequestParam(value = "invitationCode", required = false) String invitationCode) {

        generalUserService.signUp(generalUserDTO);

        // 만약 초대 코드가 전달되면서 들어온 회원가입 요청이라면 회원가입과 동시에 자동으로 해당 그룹 멤버 추가
        if (invitationCode != null && !invitationCode.isEmpty()) {
            try {
                var invitation = groupInvitationService.getValidInvitation(invitationCode);
                GroupAddMemberRequestDTO dto = new GroupAddMemberRequestDTO();
                dto.setGroupName(invitation.getGroup().getGroupName());
                // 일반 유저 가입 시, 식별자로 userIdentifier 사용
                dto.setUserIdentifier("gen_" + generalUserDTO.getEmail());
                groupService.addMemberToGroup(dto);
            } catch (Exception e) {
                System.out.println("자동 그룹 가입 실패: " + e.getMessage());
                throw new IllegalArgumentException("초대 코드가 유효하지 않습니다." + e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "회원 가입 성공"));
    }

    // 비밀번호 변경
    @PutMapping("/passwordUpdate")
    public ResponseEntity<CommonResponse<String>> updatePassword(@RequestBody GeneralUserUpdatePasswordDTO generalUserUpdatePasswordDTO) {
        try {
            generalUserService.updatePassword(generalUserUpdatePasswordDTO);
            return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "비밀번호 변경 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.error(500, e.getMessage()));
        }
    }

    // 회원 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse<GeneralUserDTO>> getProfile(Authentication authentication) {
        // 현재 로그인된 사용자 이메일 가져오기
        String email = authentication.getName();

        // 회원 정보를 조회
        GeneralUserDTO userProfile = generalUserService.getProfile(email);

        return ResponseEntity.ok(CommonResponse.success(userProfile, "회원 프로필 조회 성공"));
    }
}