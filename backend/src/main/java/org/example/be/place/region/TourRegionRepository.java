package org.example.be.place.region;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TourRegionRepository extends JpaRepository<TourRegion, Long> {

	// areaCode, siGunGuCode 조합으로 지역 찾기에 이용
	Optional<TourRegion> findByAreaCodeAndSiGunGuCode(String areaCode, String siGunGuCode);

	// 지역으로 areaCode, siGunGuCode 조합 찾기에 이용
	Optional<TourRegion> findByRegion(String region);

	boolean existsByRegion(String region);

	// 전체 지역 순회에 이용
	@Query("SELECT DISTINCT t.areaCode FROM TourRegion t")
	List<String> findDistinctAreaCode();
}
