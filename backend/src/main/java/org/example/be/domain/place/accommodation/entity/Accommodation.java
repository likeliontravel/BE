package org.example.be.domain.place.accommodation.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import org.example.be.domain.place.entity.Place;
import org.example.be.domain.place.region.TourRegion;
import org.example.be.domain.place.theme.PlaceCategory;

@Entity
@Table(name = "accommodation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Accommodation extends Place {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tour_region_id")
	private TourRegion tourRegion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "place_category_id")
	private PlaceCategory placeCategory;

	// Place 클래스의 모든 필드 상속
}
