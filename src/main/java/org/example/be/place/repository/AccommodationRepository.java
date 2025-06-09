package org.example.be.place.repository;

import org.example.be.place.entity.Accommodation;
import org.example.be.place.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    // 특정 지역 코드(areaCode)에 해당하는 관광지 목록 조회
    List<Accommodation> findByAreaCode(String areaCode);

    // contentId 중복 체크용
    boolean existsByContentId(String contentId);
}

