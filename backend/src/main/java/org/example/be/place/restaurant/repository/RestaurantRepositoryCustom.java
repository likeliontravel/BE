package org.example.be.place.restaurant.repository;

import org.example.be.place.restaurant.entity.Restaurant;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RestaurantRepositoryCustom {
    List<Restaurant> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);

//    List<Restaurant> findByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);
}
