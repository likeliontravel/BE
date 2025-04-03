package org.example.be.tour.repository;

import org.example.be.tour.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    Optional<Accommodation> findByContentId(Long contentId);
}
