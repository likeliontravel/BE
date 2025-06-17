package org.example.be.place.region;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TourRegionService {

    private final TourRegionRepository tourRegionRepository;

    public boolean existsByRegion(String region) {
        return tourRegionRepository.existsByRegion(region);
    }
}
