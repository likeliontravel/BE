package org.example.be.place.region;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourRegionRepository extends JpaRepository<TourRegion, String> {

    // 시군구 코드로 TourRegion 객체 가져오기
    Optional<TourRegion> findByAreaCodeAndSiGunGuCode(String areaCode, String siGunGuCode);

    // 지원하는 지역으로 TourRegion 전부 가져오기
    List<TourRegion> findAllByRegion(String region);

    // 지역을 입력해 db에 있는지 확인
    boolean existsByRegion(String region);
}
