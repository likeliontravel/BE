package org.example.be.domain.place.touristspot.repository;

import java.util.List;
import java.util.Optional;

import org.example.be.domain.place.touristspot.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long>, TouristSpotRepositoryCustom {
	// 기존 사용하던 Jpa 메서드들은 그대로 둔다.
	boolean existsByContentId(String contentId);

	Optional<TouristSpot> findByContentId(String contentId);

	// 일정 조회 시 N+1 해결을 위한 벌크 조회 메서드
	List<TouristSpot> findAllByContentIdIn(List<String> contentIds);

}
