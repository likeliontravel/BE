package org.example.be.Tourapi.region;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TourRegionRepository extends JpaRepository<TourRegion, Long> {

    // areaCode, siGunGuCode 조합으로 지역 찾기에 이용
    Optional<TourRegion> findByAreaCodeAndSiGunGuCode(String areaCode, String siGunGuCode);

    // 지역으로 areaCode, siGunGuCode 조합 찾기에 이용
    Optional<TourRegion> findByRegion(String region);
}
