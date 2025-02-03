package org.example.be.group.invitation.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.group.invitation.dto.InvitationResponseDTO;
import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.repository.GroupRepository;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
// 그룹 초대 링크 보기 및 생성에 관련한 컨트롤러
public class GroupInvitationController {

    private final GroupInvitationService invitationService;
    private final GroupRepository groupRepository;

    // 그룹 창설자만 초대 링크 생성 및 조회 가능. ('초대 링크 보기'버튼 누를 시 작동하도록 설계)
    // 테스트 방법은 http://localhost:8080/group/멋사/invitation?generateNew=false 이런식으로 하면 됨.
    // 마지막 false는 이미 생성된 링크가 있어 창에 링크를 표시하기 위해 존재하며,
    // true일 경우 두 가지 분기로 나뉘어 처리된다.
        // 1. 유효한 링크가 있는데 생성하려는 경우 기존 링크를 파기하고 새로운 링크를 발급하여 노출해준다.
        // 2. 유효한 링크가 없어 생성하는 경우 새로 생성된 링크가 나타나게 된다.
    // 현재 컨트롤러 세팅에 의하면 Authentication객체에 해당하는 부분은 jwt토큰이 된다. 쿠키 관련 회의 후 다시 결정해야할 듯 하다. (요청 클라이언트 기억의 문제가 아니라 기존 회원 인증시스템의 문제임)
    @PostMapping("/{groupName}/invitation")
    public ResponseEntity<CommonResponse<InvitationResponseDTO>> generateInvitation(
            @PathVariable String groupName,
            @RequestParam(required = false, defaultValue = "false") boolean generateNew,
            Authentication authentication
    ) {
        String userIdentifier = authentication.getName();
        if (!groupRepository.findByGroupName(groupName)
                .filter(group -> group.getCreatedBy().getUserIdentifier().equals(userIdentifier))
                .isPresent()) {
            throw new IllegalArgumentException("해당 그룹의 창설자만 초대 링크를 보거나 생성할 수 있습니다.");
        }
        GroupInvitation invitation = invitationService.generateInvitation(groupName, userIdentifier, generateNew);
        InvitationResponseDTO dto = new InvitationResponseDTO();
        dto.setInvitationCode("https://localhost:8080/invite/" + invitation.getInvitationCode());
        dto.setExpiresAt(invitation.getExpiresAt());
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(dto, "초대 링크가 생성되었습니다."));
    }

}
