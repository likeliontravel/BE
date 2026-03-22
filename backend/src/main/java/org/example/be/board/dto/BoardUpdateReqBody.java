package org.example.be.board.dto;

public record BoardUpdateReqBody(
	String title,
	String content,
	String theme,
	String region,
	String thumbnailPublicUrl
) {
}
