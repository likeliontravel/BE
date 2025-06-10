package org.example.be.tour.repository;


import org.example.be.tour.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {
    // 특정 지역 코드(areaCode)에 해당하는 관광지 목록 조회
    List<TouristSpot> findByAreaCode(String areaCode);

    // contentId 중복 체크용
    boolean existsByContentId(String contentId);
}
