package org.example.be.domain.place.restaurant.dto;

import org.example.be.domain.place.restaurant.entity.Restaurant;

import lombok.Builder;

@Builder
public record RestaurantResBody(
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
	public static RestaurantResBody from(Restaurant restaurant) {
		String region = (restaurant.getTourRegion() != null) ? restaurant.getTourRegion().getRegion() : "기타";
		String theme = (restaurant.getPlaceCategory() != null) ? restaurant.getPlaceCategory().getTheme() : "기타";

		return RestaurantResBody.builder()
			.contentId(restaurant.getContentId())
			.title(restaurant.getTitle())
			.addr1(restaurant.getAddr1())
			.addr2(restaurant.getAddr2())
			.areaCode(restaurant.getAreaCode())
			.siGunGuCode(restaurant.getSiGunGuCode())
			.cat1(restaurant.getCat1())
			.cat2(restaurant.getCat2())
			.cat3(restaurant.getCat3())
			.imageUrl(restaurant.getImageUrl())
			.thumbnailImageUrl(restaurant.getThumbnailImageUrl())
			.mapX(restaurant.getMapX())
			.mapY(restaurant.getMapY())
			.mLevel(restaurant.getMLevel())
			.tel(restaurant.getTel())
			.createdTime(restaurant.getCreatedTime())
			.modifiedTime(restaurant.getModifiedTime())
			.theme(theme)
			.region(region)
			.build();
	}
}
