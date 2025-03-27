package org.example.test.repository;

import org.example.test.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {
    List<TouristSpot> findByAreaCode(String areaCode);
}