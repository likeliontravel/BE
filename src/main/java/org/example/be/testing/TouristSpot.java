package org.example.be.testing;

import jakarta.persistence.*;
        import lombok.*;

@Entity
@Table(name = "tourist_spot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentId;    // 콘텐츠 ID
    private String title;        // 관광지 이름
    private String address;      // 주소
    private String areaCode;     // 지역 코드
    private String sigunguCode;  // 시군구 코드
    private String category1;    // 대분류
    private String category2;    // 중분류
    private String category3;    // 소분류
    private String imageUrl;     // 대표 이미지 URL (원본)
    private String thumbnailUrl; // 대표 이미지 URL (썸네일)
    private Double mapX;         // GPS X 좌표 (경도)
    private Double mapY;         // GPS Y 좌표 (위도)
    private String phone;        // 전화번호
    private String modifiedTime; // 수정일
    private String createdTime;  // 등록일
}
