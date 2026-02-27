package org.example.be.group.announcement.dto;

import jakarta.validation.constraints.NotBlank;

// 공지 생성 요청용 RequestBody
public record GroupAnnouncementCreateReqBody(
	@NotBlank(message = "그룹 이름은 필수입니다.")
	String groupName,
	@NotBlank(message = "공지 제목은 필수입니다.")
	String title,
	String content
) {
}
