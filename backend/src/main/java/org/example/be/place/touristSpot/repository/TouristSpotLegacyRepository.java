package org.example.be.place.touristSpot.repository;

import org.example.be.place.touristSpot.entity.TouristSpot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TouristSpotLegacyRepository extends Repository<TouristSpot, Long> {

    @Query("""
            SELECT t FROM TouristSpot t
                        WHERE (:regions IS NULL OR CONCAT(t.areaCode, t.siGunGuCode) IN (
                            SELECT CONCAT(tr.areaCode, tr.siGunGuCode)
                            FROM TourRegion tr
                            WHERE tr.region IN :regions
                                )
                            )
                            AND (:themes IS NULL OR t.cat3 IN (
                                SELECT pc.cat3
                                FROM PlaceCategory pc
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
            @Param("regions") List<String> regions,
            @Param("themes") List<String> themes,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
