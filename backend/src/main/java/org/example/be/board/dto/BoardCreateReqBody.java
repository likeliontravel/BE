package org.example.be.board.dto;

import jakarta.validation.constraints.NotBlank;

public record BoardCreateReqBody(
	@NotBlank(message = "제목은 필수입니다.")
	String title,
	@NotBlank(message = "내용은 필수입니다.")
	String content,
	@NotBlank(message = "테마는 필수입니다.")
	String theme,
	@NotBlank(message = "지역은 필수입니다.")
	String region,
	String thumbnailPublicUrl
) {
}
