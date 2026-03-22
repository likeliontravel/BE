package org.example.be.board.repository;

import static org.example.be.board.entity.QBoard.*;
import static org.example.be.member.entity.QMember.*;

import java.util.List;

import org.example.be.board.dto.BoardSearchReqBody;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.BoardSortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Board> search(BoardSearchReqBody reqBody, Pageable pageable) {
		/**
		 * 데이터 조회
		 */
		List<Board> content = queryFactory
			.selectFrom(board)
			.join(board.writer, member).fetchJoin()
			.where(
				themeEq(reqBody.theme()),
				regionEq(reqBody.region()),
				keywordContains(reqBody.searchKeyword())
			)
			.orderBy(getSortOrder(reqBody.boardSortType()))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
		/**
		 * 전체 데이터 수 조회
		 */
		Long total = queryFactory
			.select(board.count())
			.from(board)
			.where(
				themeEq(reqBody.theme()),
				regionEq(reqBody.region()),
				keywordContains(reqBody.searchKeyword())
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	//BooleanExpression 은 Querydsl에서 where 절의 조건식 자세를 표현하는 클래스입니당
	private BooleanExpression themeEq(String theme) {
		return (theme != null && !theme.isEmpty()) ? board.theme.eq(theme) : null;
	}

	private BooleanExpression regionEq(String region) {
		return (region != null && !region.isEmpty()) ? board.region.eq(region) : null;
	}

	private BooleanExpression keywordContains(String keyword) {
		if (keyword == null || keyword.isEmpty())
			return null;
		return board.title.contains(keyword)
			.or(board.content.contains(keyword))
			.or(board.writer.name.contains(keyword));
	}

	//OrderSpecifier 는 order by 절에서 사용할 수 있는 객체로, 정렬 기준과 방향을 지정하는데 사용하는 클래스 입니당
	private OrderSpecifier<?> getSortOrder(BoardSortType boardSortType) {
		if (boardSortType == BoardSortType.RECENT) {
			return board.updatedTime.desc();
		}
		// 기본값은 인기순으로 조회 ( 조회수 기준 )
		return board.boardHits.desc();
	}

}
