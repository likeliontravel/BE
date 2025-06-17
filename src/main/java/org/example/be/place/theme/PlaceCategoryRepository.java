package org.example.be.place.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, String> {
    // cat3 (PK)로 테마 얻을 때 이용
    Optional<PlaceCategory> findByCat3(String cat3);

    boolean existsPlaceCategoryByTheme(String theme);
}
