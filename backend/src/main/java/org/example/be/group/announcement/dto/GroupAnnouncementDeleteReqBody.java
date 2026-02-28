package org.example.be.group.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 공지 삭제 요청용 RequestBody
public record GroupAnnouncementDeleteReqBody(
	@NotNull(message = "공지 ID는 필수입니다.")
	Long id,
	@NotBlank(message = "그룹 이름은 필수입니다.")
	String groupName
) {
}
