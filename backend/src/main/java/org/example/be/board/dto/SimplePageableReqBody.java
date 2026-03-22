package org.example.be.board.dto;

import org.example.be.board.entity.BoardSortType;

public record SimplePageableReqBody(
	Integer page,
	Integer size,
	BoardSortType boardSortType
) {
}
