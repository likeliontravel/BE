package org.example.be.place.place_category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, String> {

    // 소분류코드로 객체 가져오기
    public Optional<PlaceCategory> findByCat3(String cat3);

    // 테마로 객체 전부 가져오기
    public List<PlaceCategory> findAllByTheme(String theme);

    // 모든 테마 가져오기
    public List<String> findDistinctTheme();
}
