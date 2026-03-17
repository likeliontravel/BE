package org.example.be.board.repository;

import java.util.List;

import org.example.be.board.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {
	Page<Comment> findRootComments(Long boardId, Pageable pageable);

	List<Comment> findChildrenByParentIds(List<Long> parentIds);
}
