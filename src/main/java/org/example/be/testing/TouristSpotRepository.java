package org.example.be.testing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {
    List<TouristSpot> findByAreaCode(String areaCode);
}
