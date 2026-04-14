package org.example.be.domain.place.touristspot.entity;

import org.example.be.domain.place.common.entity.Place;
import org.example.be.domain.place.region.TourRegion;
import org.example.be.domain.place.theme.PlaceCategory;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tourist_spot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TouristSpot extends Place {
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
