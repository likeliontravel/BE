package org.example.be.domain.place.restaurant.repository;

import java.util.List;

import org.example.be.domain.place.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepositoryCustom {
	Page<Restaurant> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);

	//    List<Restaurant> findByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);
}
