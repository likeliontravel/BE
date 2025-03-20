package org.example.test.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristAttractionDto {
    private String contentId;      // 콘텐츠 ID
    private String title;          // 관광지 이름
    private String address;        // 주소
    private String areaCode;       // 지역 코드
    private String sigunguCode;    // 시군구 코드
    private String category1;      // 대분류 (cat1)
    private String category2;      // 중분류 (cat2)
    private String category3;      // 소분류 (cat3)
    private String imageUrl;       // 대표 이미지 URL (원본)
    private String thumbnailUrl;   // 대표 이미지 URL (썸네일)
    private String mapX;           // GPS X 좌표 (경도)
    private String mapY;           // GPS Y 좌표 (위도)
    private String phone;          // 전화번호
    private String modifiedTime;   // 수정일
    private String createdTime;    // 등록일
}