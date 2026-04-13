package org.example.be.domain.board.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateReqBody(
	@NotBlank(message = "댓글 내용은 필수입니다.")
	String content
) {
}
