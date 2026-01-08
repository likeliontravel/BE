package org.example.be.place.touristSpot.repository;

import org.example.be.place.touristSpot.entity.TouristSpot;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TouristSpotRepositoryCustom {
    List<TouristSpot> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);
}
