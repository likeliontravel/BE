package org.example.be.place.accommodation.repository;

import org.example.be.place.accommodation.entity.Accommodation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    // contentId 중복 체크용
    boolean existsByContentId(String contentId);


    // 지역 또는 테마 또는 키워드로 필터링
    @Query("""
                SELECT a FROM Accommodation a
                WHERE (:regions IS NULL OR CONCAT(a.areaCode, a.siGunGuCode) IN (
                    SELECT CONCAT(tr.areaCode, tr.siGunGuCode)
                    FROM TourRegion tr
                    WHERE tr.region IN :regions
                        )
                    )
                    AND (:themes IS NULL OR a.cat3 IN (
                        SELECT pc.cat3
                        FROM PlaceCategory pc
                        WHERE pc.contentTypeId = '32' AND pc.theme IN :themes
                    ))
                    AND (
                        (:keyword IS NULL) OR
                        LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(a.addr1) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(a.addr2) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
            """)
    List<Accommodation> findByFilters(
            @Param("regions") List<String> regions,
            @Param("themes") List<String> themes,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}

