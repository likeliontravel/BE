package org.example.be.place.restaurant.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.be.place.accommodation.entity.Accommodation;
import org.example.be.place.region.QTourRegion;
import org.example.be.place.restaurant.entity.QRestaurant;
import org.example.be.place.restaurant.entity.Restaurant;
import org.example.be.place.theme.QPlaceCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Restaurant> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable) {

        QRestaurant restaurant = QRestaurant.restaurant;
        QTourRegion tourRegion = QTourRegion.tourRegion;
        QPlaceCategory placeCategory = QPlaceCategory.placeCategory;

        return queryFactory.selectFrom(restaurant)
                .leftJoin(restaurant.tourRegion, tourRegion).fetchJoin()
                .leftJoin(restaurant.placeCategory, placeCategory).fetchJoin()
                .where(
                        regions != null && !regions.isEmpty() ? tourRegion.region.in(regions) : null,
                        themes != null && !themes.isEmpty() ? placeCategory.theme.in(themes) : null,
                        StringUtils.hasText(keyword) ?
                                restaurant.title.containsIgnoreCase(keyword)
                                        .or(restaurant.addr1.containsIgnoreCase(keyword))
                                        .or(restaurant.addr2.containsIgnoreCase(keyword)) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

}
