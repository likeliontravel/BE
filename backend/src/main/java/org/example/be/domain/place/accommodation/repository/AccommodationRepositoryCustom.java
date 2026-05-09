package org.example.be.domain.place.accommodation.repository;

import java.util.List;

import org.example.be.domain.place.accommodation.entity.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccommodationRepositoryCustom {
	Page<Accommodation> findAllByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);

	//    List<Accommodation> findByFilters(List<String> regions, List<String> themes, String keyword, Pageable pageable);
}
