package org.example.be.place.accommodation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.be.place.accommodation.entity.Accommodation;
import org.example.be.place.accommodation.entity.QAccommodation;
import org.example.be.place.region.QTourRegion;
import org.example.be.place.theme.QPlaceCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class AccommodationRepositoryImpl implements AccommodationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Accommodation> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable) {

        QAccommodation accommodation = QAccommodation.accommodation;
        QTourRegion tourRegion = QTourRegion.tourRegion;
        QPlaceCategory placeCategory = QPlaceCategory.placeCategory;

        return queryFactory.selectFrom(accommodation)
                .leftJoin(accommodation.tourRegion, tourRegion).fetchJoin()
                .leftJoin(accommodation.placeCategory, placeCategory).fetchJoin()
                .where(
                        regions != null && !regions.isEmpty() ? tourRegion.region.in(regions) : null,
                        themes != null && !themes.isEmpty() ? placeCategory.theme.in(themes) : null,
                        StringUtils.hasText(keyword) ?
                                accommodation.title.containsIgnoreCase(keyword)
                                        .or(accommodation.addr1.containsIgnoreCase(keyword))
                                        .or(accommodation.addr2.containsIgnoreCase(keyword)) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();



    }



}
