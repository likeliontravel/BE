package org.example.be.tour_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SigunguDTO {

	private String areaCode;
	private String siGunGuCode;
	private String siGunGuName;

}
