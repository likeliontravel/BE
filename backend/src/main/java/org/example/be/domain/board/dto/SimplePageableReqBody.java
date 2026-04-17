package org.example.be.domain.board.dto;

import org.example.be.domain.board.entity.BoardSortType;

public record SimplePageableReqBody(
	Integer page,
	Integer size,
	BoardSortType boardSortType
) {
}
