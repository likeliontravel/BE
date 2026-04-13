package org.example.be.domain.place.touristspot.repository;

import org.example.be.domain.place.touristspot.entity.TouristSpot;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TouristSpotRepositoryCustom {
	List<TouristSpot> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);
}
