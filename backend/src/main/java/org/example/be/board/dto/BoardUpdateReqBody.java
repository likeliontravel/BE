package org.example.be.board.dto;

import jakarta.validation.constraints.NotNull;

public record BoardUpdateReqBody(
	@NotNull(message = "게시글 ID는 필수입니다.")
	Long id,
	String title,
	String content,
	String theme,
	String region,
	String thumbnailPublicUrl
) {
}
