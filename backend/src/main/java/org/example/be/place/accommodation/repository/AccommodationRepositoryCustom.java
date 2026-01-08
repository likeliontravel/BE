package org.example.be.place.accommodation.repository;

import org.example.be.place.accommodation.entity.Accommodation;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccommodationRepositoryCustom {
    List<Accommodation> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);

//    List<Accommodation> findByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);
}
