package org.example.be.tour.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristSpotDTO {
    private String contentid;
    private String title;
    private String addr1;
    private String areacode;
    private String sigungucode;
    private String cat1;
    private String cat2;
    private String cat3;
    private String firstimage;
    private String firstimage2;
    private Double mapx;
    private Double mapy;
    private String tel;
    private String modifiedtime;
    private String createdtime;
}
