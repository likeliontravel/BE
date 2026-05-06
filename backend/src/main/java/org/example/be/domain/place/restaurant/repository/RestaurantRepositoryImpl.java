package org.example.be.domain.place.restaurant.repository;

import java.util.List;

import org.example.be.domain.place.region.QTourRegion;
import org.example.be.domain.place.restaurant.entity.QRestaurant;
import org.example.be.domain.place.restaurant.entity.Restaurant;
import org.example.be.domain.place.theme.QPlaceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Restaurant> findAllByFilters(List<String> regions, List<String> themes, String keyword,
		Pageable pageable) {

		QRestaurant restaurant = QRestaurant.restaurant;
		QTourRegion tourRegion = QTourRegion.tourRegion;
		QPlaceCategory placeCategory = QPlaceCategory.placeCategory;

		List<Restaurant> content = queryFactory.selectFrom(restaurant)
			.leftJoin(restaurant.tourRegion, tourRegion).fetchJoin()
			.leftJoin(restaurant.placeCategory, placeCategory).fetchJoin()
			.where(
				regionsIn(regions, tourRegion),
				themesIn(themes, placeCategory),
				keywordContains(keyword, restaurant)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory.select(restaurant.count())
			.from(restaurant)
			.leftJoin(restaurant.tourRegion, tourRegion)
			.leftJoin(restaurant.placeCategory, placeCategory)
			.where(
				regionsIn(regions, tourRegion),
				themesIn(themes, placeCategory),
				keywordContains(keyword, restaurant)
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	public BooleanExpression regionsIn(List<String> regions, QTourRegion tourRegion) {
		return regions != null && !regions.isEmpty() ? tourRegion.region.in(regions) : null;
	}

	public BooleanExpression themesIn(List<String> themes, QPlaceCategory placeCategory) {
		return themes != null && !themes.isEmpty() ? placeCategory.theme.in(themes) : null;
	}

	private BooleanExpression keywordContains(String keyword, QRestaurant restaurant) {
		return StringUtils.hasText(keyword) ?
			restaurant.title.containsIgnoreCase(keyword)
				.or(restaurant.addr1.containsIgnoreCase(keyword))
				.or(restaurant.addr2.containsIgnoreCase(keyword)) : null;
	}
}
