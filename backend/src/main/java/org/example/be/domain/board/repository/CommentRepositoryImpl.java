package org.example.be.domain.board.repository;

import static org.example.be.domain.board.entity.QComment.*;
import static org.example.be.domain.member.entity.QMember.*;

import java.util.List;

import org.example.be.domain.board.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Comment> findRootComments(Long boardId, Pageable pageable) {
		List<Comment> contents = queryFactory
			.selectFrom(comment)
			.join(comment.writer, member).fetchJoin()
			.where(
				comment.board.id.eq(boardId),
				comment.parentComment.isNull() // 부모가 없는 댓글만 조회
			)
			.orderBy(comment.createdTime.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(comment.count())
			.from(comment)
			.where(
				comment.board.id.eq(boardId),
				comment.parentComment.isNull()
			)
			.fetchOne();

		return new PageImpl<>(contents, pageable, total != null ? total : 0L);

	}

	@Override
	public List<Comment> findChildrenByParentIds(List<Long> parentIds) {
		return queryFactory
			.selectFrom(comment)
			.join(comment.writer, member).fetchJoin()
			.where(comment.parentComment.id.in(parentIds))
			.orderBy(comment.createdTime.asc())
			.fetch();
	}

}
