package org.example.test.repository;

import org.example.test.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Location findByAreaCode(int areaCode);  // 특정 areaCode로 Location을 찾는 메서드
}