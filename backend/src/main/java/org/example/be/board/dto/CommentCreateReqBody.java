package org.example.be.board.dto;

import jakarta.validation.constraints.NotNull;

public record CommentCreateReqBody(
	@NotNull(message = "게시글 ID는 필수입니다.")
	Long boardId,
	@NotNull(message = "댓글 내용은 필수입니다.")
	String content,
	Long parentCommentId // 대댓글 작성 시 부모 댓글 ID, 일반 댓글 작성 시 null
) {
}
