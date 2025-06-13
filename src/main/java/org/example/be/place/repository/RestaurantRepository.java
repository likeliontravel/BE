package org.example.be.place.repository;

import org.example.be.place.entity.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    boolean existsByContentId(String contentId);

    // 지역 또는 테마 또는 키워드로 필터링해서 조회
    @Query("""
                SELECT t FROM Restaurant t
                WHERE (:regions IS NULL OR CONCAT(t.areaCode, t.siGunGuCode) IN (
                    SELECT CONCAT(tr.areaCode, tr.siGunGuCode)
                    FROM TourRegion tr
                    WHERE tr.region IN :regions
                        )
                    )
                    AND (:themes IS NULL OR t.cat3 IN (
                        SELECT pc.cat3
                        FROM PlaceCategory pc
                        WHERE pc.contentTypeId = '39' AND pc.theme IN :themes
                    ))
                    AND (
                        (:keyword IS NULL) OR
                        LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(t.addr1) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(t.addr2) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
            """)
    List<Restaurant> findByFilters(
            @Param("regions") List<String> regions,
            @Param("themes") List<String> themes,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
