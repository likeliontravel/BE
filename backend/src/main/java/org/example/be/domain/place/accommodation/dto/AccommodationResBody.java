package org.example.be.domain.place.accommodation.dto;

import org.example.be.domain.place.accommodation.entity.Accommodation;

import lombok.Builder;

@Builder
public record AccommodationResBody(
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
	public static AccommodationResBody from(Accommodation accommodation) {
		String region = (accommodation.getTourRegion() != null) ? accommodation.getTourRegion().getRegion() : "기타";
		String theme = (accommodation.getPlaceCategory() != null) ? accommodation.getPlaceCategory().getTheme() : "기타";

		return AccommodationResBody.builder()
			.contentId(accommodation.getContentId())
			.title(accommodation.getTitle())
			.addr1(accommodation.getAddr1())
			.addr2(accommodation.getAddr2())
			.areaCode(accommodation.getAreaCode())
			.siGunGuCode(accommodation.getSiGunGuCode())
			.cat1(accommodation.getCat1())
			.cat2(accommodation.getCat2())
			.cat3(accommodation.getCat3())
			.imageUrl(accommodation.getImageUrl())
			.thumbnailImageUrl(accommodation.getThumbnailImageUrl())
			.mapX(accommodation.getMapX())
			.mapY(accommodation.getMapY())
			.mLevel(accommodation.getMLevel())
			.tel(accommodation.getTel())
			.createdTime(accommodation.getCreatedTime())
			.modifiedTime(accommodation.getModifiedTime())
			.theme(theme)
			.region(region)
			.build();
	}
}
