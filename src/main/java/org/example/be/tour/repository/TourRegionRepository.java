package org.example.be.tour.repository;


import org.example.be.tour.entity.TourRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRegionRepository extends JpaRepository<TourRegion, Long> {
    //List<TourRegion> findByAreaCode(int areaCode);
    Optional<TourRegion> findFirstByAreaCode(int areaCode);
}
