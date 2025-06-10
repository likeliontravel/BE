package org.example.be.tour.repository;

import org.example.be.tour.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {


    boolean existsByContentId(String contentId);
}
