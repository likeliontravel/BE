package org.example.be.board.dto;

import java.time.LocalDateTime;

public record CommentResBody(
	Long id,
	String writer,
	String commentWriterProfileImageUrl,
	String commentContent,
	Long boardId,
	Long parentCommentId,
	LocalDateTime commentCreatedTime

) {
}
