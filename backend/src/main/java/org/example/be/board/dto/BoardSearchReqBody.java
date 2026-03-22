package org.example.be.board.dto;

import org.example.be.board.entity.BoardSortType;

public record BoardSearchReqBody(
	String theme,
	String region,
	String searchKeyword,
	BoardSortType boardSortType,
	Integer page,
	Integer size
) {
}
