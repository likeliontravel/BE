package org.example.be.domain.place.accommodation.repository;

import java.util.List;

import org.example.be.domain.place.accommodation.entity.Accommodation;
import org.example.be.domain.place.accommodation.entity.QAccommodation;
import org.example.be.domain.place.region.QTourRegion;
import org.example.be.domain.place.theme.QPlaceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccommodationRepositoryImpl implements AccommodationRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Accommodation> findAllByFilters(List<String> regions, List<String> themes, String keyword,
		Pageable pageable) {

		QAccommodation accommodation = QAccommodation.accommodation;
		QTourRegion tourRegion = QTourRegion.tourRegion;
		QPlaceCategory placeCategory = QPlaceCategory.placeCategory;

		List<Accommodation> content = queryFactory.selectFrom(accommodation)
			.leftJoin(accommodation.tourRegion, tourRegion).fetchJoin()
			.leftJoin(accommodation.placeCategory, placeCategory).fetchJoin()
			.where(
				regionsIn(regions, tourRegion),
				themesIn(themes, placeCategory),
				keywordContains(keyword, accommodation)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory.select(accommodation.count())
			.from(accommodation)
			.leftJoin(accommodation.tourRegion, tourRegion)
			.leftJoin(accommodation.placeCategory, placeCategory)
			.where(
				regionsIn(regions, tourRegion),
				themesIn(themes, placeCategory),
				keywordContains(keyword, accommodation)
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

	private BooleanExpression keywordContains(String keyword, QAccommodation accommodation) {
		return StringUtils.hasText(keyword) ?
			accommodation.title.containsIgnoreCase(keyword)
				.or(accommodation.addr1.containsIgnoreCase(keyword))
				.or(accommodation.addr2.containsIgnoreCase(keyword)) : null;
	}
}
