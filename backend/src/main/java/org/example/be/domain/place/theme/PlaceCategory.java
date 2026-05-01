package org.example.be.domain.place.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "place_category")
public class PlaceCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "cat3", unique = true, nullable = false)
	private String cat3;    // 소분류 코드를 PK로 사용. ** Long Id 아님! **

	@Column(name = "contentTypeId")
	private String contentTypeId;

	@Column(name = "cat1")
	private String cat1;

	@Column(name = "cat2")
	private String cat2;

	@Column(name = "large_classification")
	private String largeClassification;

	@Column(name = "mid_classification")
	private String midClassification;

	@Column(name = "small_classification")
	private String smallClassification;

	@Column(name = "theme")
	private String theme;
}
