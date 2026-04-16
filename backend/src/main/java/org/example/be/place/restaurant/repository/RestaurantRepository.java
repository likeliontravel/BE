package org.example.be.place.restaurant.repository;

import java.util.List;
import java.util.Optional;

import org.example.be.place.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantRepositoryCustom {

	boolean existsByContentId(String contentId);

	// 일정 생성 할때 식당의 contentId를 기준으로 가져옴
	Optional<Restaurant> findByContentId(String contentId);

	// 일정 조회 시 N+1 해결을 위한 벌크 조회 메서드
	List<Restaurant> findAllByContentIdIn(List<String> contentIds);

}
