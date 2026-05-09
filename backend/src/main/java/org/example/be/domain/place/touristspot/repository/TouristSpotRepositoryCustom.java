package org.example.be.domain.place.touristspot.repository;

import org.example.be.domain.place.touristspot.entity.TouristSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TouristSpotRepositoryCustom {
	Page<TouristSpot> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);
}
