package org.example.be.group.dto;

import jakarta.validation.constraints.NotBlank;

// 그룹 나가기, 그룹 삭제 요청용 RequestBody
public record GroupExitOrDeleteReqBody(
	@NotBlank(message = "그룹 이름은 필수입니다.")
	String groupName
) {
}
