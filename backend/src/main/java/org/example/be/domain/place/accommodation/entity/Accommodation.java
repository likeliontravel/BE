package org.example.be.domain.place.accommodation.entity;

import org.example.be.domain.place.region.TourRegion;
import org.example.be.domain.place.shared.entity.Place;
import org.example.be.domain.place.theme.PlaceCategory;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "accommodation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

	public void update(String title, String addr1, String addr2, String areaCode, String siGunGuCode,
		String cat1, String cat2, String cat3, String imageUrl, String thumbnailImageUrl,
		Double mapX, Double mapY, Integer mLevel, String tel, String modifiedTime,
		TourRegion tourRegion, PlaceCategory placeCategory) {

		super.updateCommonFields(title, addr1, addr2, areaCode, siGunGuCode, cat1, cat2, cat3,
			imageUrl, thumbnailImageUrl, mapX, mapY, mLevel, tel, modifiedTime);

		this.tourRegion = tourRegion;
		this.placeCategory = placeCategory;
	}
}
