package org.example.be.tour.repository;

import org.example.be.tour.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByLocation_AreaCode(int areaCode);  // areaCode에 맞는 맛집을 찾는 메서드
}
