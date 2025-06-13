package org.example.be.group.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// 임시 DTO 클래스. 멤버초대 구현 후 변경 또는 삭제될 예정
public class GroupAddMemberRequestDTO {
    private String groupName;
    private String userIdentifier;
}
