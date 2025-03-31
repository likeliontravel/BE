package org.example.be.place.place_category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, String> {

    // 소분류코드로 객체 가져오기
    public Optional<PlaceCategory> findByCat3(String cat3);

    // 테마로 객체 전부 가져오기
    public List<PlaceCategory> findAllByTheme(String theme);

    // 모든 테마 가져오기 (중복 제거)
    @Query("SELECT DISTINCT p.theme FROM PlaceCategory p")
    List<String> findAllThemesDistinct();

    // 테마를 입력해 해당 테마가 DB에 존재하는지 확인
    boolean existsPlaceCategoryByTheme(String theme);
}
