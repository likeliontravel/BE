package org.example.test.repository;

import org.example.test.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    List<Accommodation> findByLocation_AreaCode(int areaCode);  // areaCode에 맞는 숙소를 찾는 메서드
}