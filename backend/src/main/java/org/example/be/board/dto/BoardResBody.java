package org.example.be.board.dto;

import java.time.LocalDateTime;

import org.example.be.board.entity.Board;

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
	String writerIdentifier, // 작성자 본인확인 로직 강화 - 0520 추가
	String writerProfileImageUrl
) {
	// 엔티티를 dto로 변환
	public static BoardResBody toDTO(Board board, String profileImageUrl) {
		return new BoardResBody(
			board.getId(),
			board.getTitle(),
			board.getContent(),
			board.getWriter(),
			board.getBoardHits(),
			board.getTheme(),
			board.getRegion(),
			board.getThumbnailPublicUrl(),
			board.getCreatedTime(),
			board.getUpdatedTime(),
			board.getWriterIdentifier(),
			profileImageUrl
		);
	}
}
