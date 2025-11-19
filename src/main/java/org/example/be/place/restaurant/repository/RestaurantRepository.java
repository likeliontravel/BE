package org.example.be.place.restaurant.repository;

import org.example.be.place.restaurant.entity.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantRepositoryCustom {

    boolean existsByContentId(String contentId);

    // 일정 생성 할때 식당의 contentId를 기준으로 가져옴
    Optional<Restaurant> findByContentId(String contentId);

    // 필터링 조회 메서드는 부모 인터페이스인 AccommodationRepositoryCustom의 구현체를 따라 찾음
}
