package org.example.be.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// 그룹 생성 요청용 RequestBody
public record GroupCreateReqBody(
	@NotBlank(message = "그룹 이름은 필수입니다.")
	@Pattern(
		regexp = "^[a-zA-Z0-9가-힣_-]+$",
		message = "그룹 이름에는 공백이나 특수문자를 포함할 수 없습니다. (한글, 영문, 숫자, -, _만 허용)"
	)
	String groupName,
	String description
) {
}
