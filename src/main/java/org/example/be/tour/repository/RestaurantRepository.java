package org.example.be.tour.repository;

import org.example.be.tour.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    boolean existsByContentId(String contentId);
}
