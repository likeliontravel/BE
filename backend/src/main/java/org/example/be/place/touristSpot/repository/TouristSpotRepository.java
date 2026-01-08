package org.example.be.place.touristSpot.repository;

import org.example.be.place.touristSpot.entity.TouristSpot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long>, TouristSpotRepositoryCustom {
    // 기존 사용하던 Jpa 메서드들은 그대로 둔다.
    boolean existsByContentId(String contentId);
    Optional<TouristSpot> findByContentId(String contentId);

    // 필터링 메서드는 부모인터페이스인 TouristSpotRepositoryCustom의 구현체 클래스에서 오버라이딩되어있다.
    // JPA 네이밍 전략에 따라 필터링 메서드가 호출되면
    // TouristSpotRepositoryImpl(이 인터페이스의 구현체이면서, 부모 인터페이스에서 추상메서드로 선언된 필터링 기능 메서드가 있는 곳)에서 로직이 동작한다.
}
