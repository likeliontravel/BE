package org.example.be.Tourapi.repository;


import org.example.be.Tourapi.entity.TouristSpot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {
    // contentId 중복 체크용
    boolean existsByContentId(String contentId);


    // 지역 또는 테마 또는 키워드로 필터링해서 조회
    @Query("""
                SELECT t FROM TouristSpot t
                WHERE (:regions IS NULL OR CONCAT(t.areaCode, t.siGunGuCode) IN (
                    SELECT CONCAT(tr.areaCode, tr.siGunGuCode)
                    FROM TourRegion tr
                    WHERE tr.region IN :regions
                        )
                    )
                    AND (:themes IS NULL OR t.cat3 IN (
                        SELECT pc.cat3\s
                        FROM PlaceCategory pc\s
                        WHERE pc.contentTypeId = '12' AND pc.theme IN :themes
                    ))
                    AND (
                        (:keyword IS NULL) OR
                        LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(t.addr1) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(t.addr2) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
            """)
    List<TouristSpot> findAllByFilters(
            @Param("region") List<String> regions,
            @Param("theme") List<String> themes,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
