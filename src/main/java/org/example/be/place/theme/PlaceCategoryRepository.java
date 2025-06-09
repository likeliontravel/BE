package org.example.be.place.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, String> {
    // cat3 (PK)로 테마 얻을 때 이용
    PlaceCategory findByCat3(String cat3);
}
