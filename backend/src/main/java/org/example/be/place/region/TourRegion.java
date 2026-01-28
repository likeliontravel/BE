package org.example.be.place.region;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Setter
@NoArgsConstructor
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
}
