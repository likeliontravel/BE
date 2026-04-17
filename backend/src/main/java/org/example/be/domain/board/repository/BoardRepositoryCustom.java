package org.example.be.domain.board.repository;

import org.example.be.domain.board.dto.BoardSearchReqBody;
import org.example.be.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardRepositoryCustom {
	Page<Board> search(BoardSearchReqBody reqBody, Pageable pageable);
}
