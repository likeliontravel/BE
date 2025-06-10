package org.example.be.tour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class Place {

    @Column(name = "content_id", unique = true, nullable = false)
    private String contentId;   // Tour API 콘텐츠 ID (고유값)

    private String title;       // 장소 이름

    private String addr1;     // 주소
    private String addr2;       // 세부 주소 ( 건물 내 층수나 주소설명
    private String areaCode;    // 지역 코드
    private String siGunGuCode; // 시군구 코드

    private String cat1;        // 대분류 코드
    private String cat2;        // 중분류 코드
    private String cat3;        // 소분류 코드

    private String imageUrl;    // 이미지 원본 URL
    private String thumbnailImageUrl;   // 300 X 200 조정된 이미지 URL

    private Double mapX;        // 경도 (지도 상 X좌표)
    private Double mapY;        // 위도 (지도 상 Y좌표)

    private Integer mLevel;     // 맵 확대 레벨 (nullable) : 지도에 마커로 표시할 때 초기 지도 줌 수준을 자동 조정하는데 사용된다고 합니다.

    private String tel;         // 전화번호
    private String createdTime; // 등록일
    private String modifiedTime;// 수정일
}