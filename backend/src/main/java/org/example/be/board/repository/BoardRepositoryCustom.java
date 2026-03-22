package org.example.be.board.repository;

import org.example.be.board.dto.BoardSearchReqBody;
import org.example.be.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardRepositoryCustom {
	Page<Board> search(BoardSearchReqBody reqBody, Pageable pageable);
}
