package org.example.be.domain.place.region;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tour_region",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_area_sigungu", columnNames = {"areaCode", "siGunGuCode"})
	},
	indexes = {
		@Index(name = "idx_tourregion_region", columnList = "region")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TourRegion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "areaCode")
	private String areaCode;

	@Column(name = "areaName")
	private String areaName;

	@Column(name = "siGunGuCode")
	private String siGunGuCode;

	@Column(name = "siGunGuName")
	private String siGunGuName;

	@Column(name = "region")
	private String region;

	public void update(String areaName, String siGunGuName, String region) {
		this.areaName = areaName;
		this.siGunGuName = siGunGuName;
		this.region = region;
	}
}
