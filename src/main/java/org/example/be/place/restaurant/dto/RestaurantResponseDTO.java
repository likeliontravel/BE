package org.example.be.place.restaurant.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantResponseDTO {
    private String contentId;
    private String title;
    private String imageUrl;
    private String region;
    private String theme;
    private String address;
}
