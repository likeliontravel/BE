package org.example.be.domain.place.touristspot.repository;

import java.util.List;

import org.example.be.domain.place.region.QTourRegion;
import org.example.be.domain.place.theme.QPlaceCategory;
import org.example.be.domain.place.touristspot.entity.QTouristSpot;
import org.example.be.domain.place.touristspot.entity.TouristSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TouristSpotRepositoryImpl implements TouristSpotRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<TouristSpot> findAllByFilters(List<String> regions, List<String> themes, String keyword,
		Pageable pageable) {
		QTouristSpot touristSpot = QTouristSpot.touristSpot;
		QTourRegion tourRegion = QTourRegion.tourRegion;
		QPlaceCategory placeCategory = QPlaceCategory.placeCategory;

		List<TouristSpot> content = queryFactory.selectFrom(touristSpot)
			.leftJoin(touristSpot.tourRegion, tourRegion).fetchJoin()
			.leftJoin(touristSpot.placeCategory, placeCategory).fetchJoin()
			.where(
				regionsIn(regions, tourRegion),
				themesIn(themes, placeCategory),
				keywordContains(keyword, touristSpot)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory.select(touristSpot.count())
			.from(touristSpot)
			.leftJoin(touristSpot.tourRegion, tourRegion)
			.leftJoin(touristSpot.placeCategory, placeCategory)
			.where(
				regionsIn(regions, tourRegion),
				themesIn(themes, placeCategory),
				keywordContains(keyword, touristSpot)
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	private BooleanExpression regionsIn(List<String> regions, QTourRegion tourRegion) {
		return regions != null && !regions.isEmpty() ? tourRegion.region.in(regions) : null;
	}

	private BooleanExpression themesIn(List<String> themes, QPlaceCategory placeCategory) {
		return themes != null && !themes.isEmpty() ? placeCategory.theme.in(themes) : null;
	}

	private BooleanExpression keywordContains(String keyword, QTouristSpot touristSpot) {
		return StringUtils.hasText(keyword) ?
			touristSpot.title.containsIgnoreCase(keyword)
				.or(touristSpot.addr1.containsIgnoreCase(keyword))
				.or(touristSpot.addr2.containsIgnoreCase(keyword)) : null;
	}
}
