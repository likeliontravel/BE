package org.example.be.place.touristSpot.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TouristSpotResponseDTO {
    private String contentId;
    private String title;
    private String imageUrl;    // thumbnailImageUrl을 반환. (firstimage2)
    private String region;
    private String theme;
    private String address;
}
