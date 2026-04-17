package org.example.be.domain.board.dto;

import org.example.be.domain.board.entity.BoardSortType;

public record BoardSearchReqBody(
	String theme,
	String region,
	String searchKeyword,
	BoardSortType boardSortType,
	Integer page,
	Integer size
) {
}
