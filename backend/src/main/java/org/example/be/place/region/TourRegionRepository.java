package org.example.be.place.region;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TourRegionRepository extends JpaRepository<TourRegion, Long> {

	// areaCode, siGunGuCode 조합으로 지역 찾기에 이용
	Optional<TourRegion> findByAreaCodeAndSiGunGuCode(String areaCode, String siGunGuCode);

	// 여러 개의 (areaCode, siGunGuCode) 조합으로 지역 정보를 한 번에 조회 (N+1 방지)
	@Query("SELECT t FROM TourRegion t WHERE CONCAT(t.areaCode, '-', t.siGunGuCode) IN :regionKeys")
	List<TourRegion> findAllByRegionKeys(@Param("regionKeys") Collection<String> regionKeys);

	// 지역으로 areaCode, siGunGuCode 조합 찾기에 이용
	Optional<TourRegion> findByRegion(String region);

	boolean existsByRegion(String region);

	// 전체 지역 순회에 이용
	@Query("SELECT DISTINCT t.areaCode FROM TourRegion t")
	List<String> findDistinctAreaCode();
}
