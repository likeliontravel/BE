package org.example.be.group.dto;

import jakarta.validation.constraints.NotBlank;

// 그룹 설명 수정 요청용 RequestBody
public record GroupModifyReqBody(
	@NotBlank(message = "그룹 이름은 필수입니다.")
	String groupName,
	String description
) {
}
