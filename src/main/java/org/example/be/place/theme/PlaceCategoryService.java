package org.example.be.place.theme;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceCategoryService {

    private final PlaceCategoryRepository placeCategoryRepository;

    public boolean existsByTheme(String theme) {
        return placeCategoryRepository.existsPlaceCategoryByTheme(theme);
    }
}
