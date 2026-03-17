package org.example.be.board.repository;

import java.util.List;

import org.example.be.board.entity.Comment;

public interface CommentRepositoryCustom {
	List<Comment> findAllByBoardIdWithMember(Long boardId);
}
