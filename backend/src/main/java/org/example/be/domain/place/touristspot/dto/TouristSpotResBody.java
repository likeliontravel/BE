package org.example.be.domain.place.touristspot.dto;

import org.example.be.domain.place.touristspot.entity.TouristSpot;

import lombok.Builder;

@Builder
public record TouristSpotResBody(
	String contentId,
	String title,
	String addr1,
	String addr2,
	String areaCode,
	String siGunGuCode,
	String cat1,
	String cat2,
	String cat3,
	String imageUrl,
	String thumbnailImageUrl,
	Double mapX,
	Double mapY,
	Integer mLevel,
	String tel,
	String modifiedTime,
	String createdTime,
	String theme,
	String region
) {
	public static TouristSpotResBody from(TouristSpot touristSpot) {
		String region = (touristSpot.getTourRegion() != null) ? touristSpot.getTourRegion().getRegion() : "기타";
		String theme = (touristSpot.getPlaceCategory() != null) ? touristSpot.getPlaceCategory().getTheme() : "기타";

		return TouristSpotResBody.builder()
			.contentId(touristSpot.getContentId())
			.title(touristSpot.getTitle())
			.addr1(touristSpot.getAddr1())
			.addr2(touristSpot.getAddr2())
			.areaCode(touristSpot.getAreaCode())
			.siGunGuCode(touristSpot.getSiGunGuCode())
			.cat1(touristSpot.getCat1())
			.cat2(touristSpot.getCat2())
			.cat3(touristSpot.getCat3())
			.imageUrl(touristSpot.getImageUrl())
			.thumbnailImageUrl(touristSpot.getThumbnailImageUrl())
			.mapX(touristSpot.getMapX())
			.mapY(touristSpot.getMapY())
			.mLevel(touristSpot.getMLevel())
			.tel(touristSpot.getTel())
			.createdTime(touristSpot.getCreatedTime())
			.modifiedTime(touristSpot.getModifiedTime())
			.theme(theme)
			.region(region)
			.build();
	}
}
