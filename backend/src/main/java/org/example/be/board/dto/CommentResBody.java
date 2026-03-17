package org.example.be.board.dto;

import java.time.LocalDateTime;

import org.example.be.board.entity.Comment;

public record CommentResBody(
	Long id,
	String writer,
	String commentWriterProfileImageUrl,
	String commentContent,
	Long boardId,
	Long parentCommentId,
	LocalDateTime commentCreatedTime

) {
	public static CommentResBody from(Comment comment) {
		return new CommentResBody(
			comment.getId(),
			comment.getWriter().getName(),
			comment.getWriter().getProfileImageUrl(),
			comment.getCommentContent(),
			comment.getBoard().getId(),
			comment.getParentComment() != null ? comment.getParentComment().getId() : null,
			comment.getCreatedTime()
		);
	}
}
