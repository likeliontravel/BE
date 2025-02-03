package org.example.be.generaluser.controller;

import lombok.RequiredArgsConstructor;
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
    @PostMapping("/SignUp")
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
                dto.setUserIdentifier("gen " + generalUserDTO.getEmail());
                groupService.addMemberToGroup(dto);
            } catch (Exception e) {
                System.out.println("자동 그룹 가입 실패: " + e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "회원 가입 성공"));
    }

    // 회원정보 수정 -> 마이페이지 패키지가 만들어진다면.. 옮길 필요가 있을 수도 ? 얘가 여기 있는게 맞을까?
    @PutMapping("/update")
    public ResponseEntity<CommonResponse<String>> updateUser(@RequestBody GeneralUserDTO generalUserDTO) {

        try {
            generalUserService.updateGeneralUser(generalUserDTO);
            return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "회원 수정 성공"));
        } catch (NoSuchElementException e) {
            // 회원을 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponse.error(404, e.getMessage()));
        } catch (IllegalArgumentException e) {

            // 요청 데이터가 없는 경우
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error(400, e.getMessage()));
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