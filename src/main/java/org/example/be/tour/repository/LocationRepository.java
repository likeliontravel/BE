package org.example.be.tour.repository;

import org.example.be.tour.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Location findByAreaCode(int areaCode);  // 특정 areaCode로 Location을 찾는 메서드
}
