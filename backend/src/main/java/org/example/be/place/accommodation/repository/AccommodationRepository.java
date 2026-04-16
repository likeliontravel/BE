package org.example.be.place.accommodation.repository;

import java.util.List;
import java.util.Optional;

import org.example.be.place.accommodation.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long>, AccommodationRepositoryCustom {
	// contentId 중복 체크용
	boolean existsByContentId(String contentId);

	// 일정 생성 할때 숙소의 contentId를 기준으로 가져옴
	Optional<Accommodation> findByContentId(String contentId);

	// 일정 조회 시 N+1 해결을 위한 벌크 조회 메서드
	List<Accommodation> findAllByContentIdIn(List<String> contentIds);

}

