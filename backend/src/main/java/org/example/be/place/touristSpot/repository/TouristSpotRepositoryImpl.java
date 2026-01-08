package org.example.be.place.touristSpot.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.be.place.region.QTourRegion;
import org.example.be.place.theme.QPlaceCategory;
import org.example.be.place.touristSpot.entity.QTouristSpot;
import org.example.be.place.touristSpot.entity.TouristSpot;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class TouristSpotRepositoryImpl implements TouristSpotRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TouristSpot> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable) {
        QTouristSpot touristSpot = QTouristSpot.touristSpot;
        QTourRegion tourRegion = QTourRegion.tourRegion;
        QPlaceCategory placeCategory = QPlaceCategory.placeCategory;

        return queryFactory.selectFrom(touristSpot)
                .leftJoin(touristSpot.tourRegion, tourRegion).fetchJoin()
                .leftJoin(touristSpot.placeCategory, placeCategory).fetchJoin()
                .where(
                        regions != null && !regions.isEmpty() ? tourRegion.region.in(regions) : null,
                        themes != null && !themes.isEmpty() ? placeCategory.theme.in(themes) : null,
                        StringUtils.hasText(keyword) ?
                                touristSpot.title.containsIgnoreCase(keyword)
                                        .or(touristSpot.addr1.containsIgnoreCase(keyword))
                                        .or(touristSpot.addr2.containsIgnoreCase(keyword)) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

}
