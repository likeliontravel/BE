package org.example.be.tour.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccommodationResponseDTO {
    private String contentId;
    private String title;
    private String imageUrl;    // thumbnailImageUrl을 반환. (firstimage2)
    private String region;
    private String theme;
    private String address;
}
