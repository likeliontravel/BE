package org.example.be.domain.board.dto;

import java.time.LocalDateTime;

import org.example.be.domain.board.entity.Board;

public record BoardResBody(
	Long id,
	String title,
	String content,
	String writer,
	int boardHits,
	String theme,
	String region,
	String thumbnailPublicUrl,
	LocalDateTime createdTime,
	LocalDateTime updatedTime,
	String writerProfileImageUrl
) {
	// 엔티티를 dto로 변환
	public static BoardResBody from(Board board, String profileImageUrl) {
		return new BoardResBody(
			board.getId(),
			board.getTitle(),
			board.getContent(),
			board.getWriter().getName(),
			board.getBoardHits(),
			board.getTheme(),
			board.getRegion(),
			board.getThumbnailPublicUrl(),
			board.getCreatedTime(),
			board.getUpdatedTime(),
			profileImageUrl
		);
	}
}
