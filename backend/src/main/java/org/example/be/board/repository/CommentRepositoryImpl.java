package org.example.be.board.repository;

import static org.example.be.board.entity.QComment.*;
import static org.example.be.member.entity.QMember.*;

import java.util.List;

import org.example.be.board.entity.Comment;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Comment> findAllByBoardIdWithMember(Long boardId) {
		return queryFactory
			.selectFrom(comment)
			.join(comment.writer, member).fetchJoin()
			.leftJoin(comment.parentComment).fetchJoin()
			.where(comment.board.id.eq(boardId))
			.orderBy(comment.createdTime.asc())
			.fetch();

	}

}
