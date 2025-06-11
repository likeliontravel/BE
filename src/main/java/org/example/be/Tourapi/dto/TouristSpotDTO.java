package org.example.be.Tourapi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TouristSpotDTO {
    private String contentId;
    private String title;
    private String addr1;
    private String addr2;
    private String areaCode;
    private String siGunGuCode;
    private String cat1;
    private String cat2;
    private String cat3;
    private String imageUrl;
    private String thumbnailImageUrl;
    private Double mapX;
    private Double mapY;
    private Integer mLevel;
    private String tel;
    private String modifiedTime;
    private String createdTime;
}

