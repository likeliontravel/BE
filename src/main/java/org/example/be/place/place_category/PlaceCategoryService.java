package org.example.be.place.place_category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaceCategoryService {

    private final PlaceCategoryRepository placeCategoryRepository;

    // 소분류 코드로 테마 가져오기
    public String getThemeByCat3(String cat3) {
        return placeCategoryRepository.findByCat3(cat3).map(PlaceCategory::getTheme)
                .orElseThrow(() -> new IllegalArgumentException("해당 소분류코드로 테마를 찾을 수 없습니다. cat3: " + cat3));
    }

    // 모든 테마 목록 가져오기
    public List<String> getAllThemes() {
        return placeCategoryRepository.findAllThemesDistinct();
    }

    // 테마 이름이 존재하는지 확인
    public boolean existsByTheme(String theme) {
        return placeCategoryRepository.existsPlaceCategoryByTheme(theme);
    }
}

