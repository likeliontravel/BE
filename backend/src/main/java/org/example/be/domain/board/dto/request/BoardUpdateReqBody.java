package org.example.be.domain.board.dto.request;

public record BoardUpdateReqBody(
	String title,
	String content,
	String theme,
	String region,
	String thumbnailPublicUrl
) {
}
