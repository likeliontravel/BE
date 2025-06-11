/*
package org.example.be.place.place_category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "place_category")
public class PlaceCategory {

    @Id
    @Column(name = "cat3")
    private String cat3;    // 소분류 코드 ( PK로 사용 )

    @Column(name = "contenttypeid")
    private String contentTypeId;   // TourAPI4.0에서 받은 카테고리 대분류(혹시몰라서저장해둠)

    @Column(name = "cat1")
    private String cat1;    // 대분류 코드

    @Column(name = "cat2")
    private String cat2;    // 중분류 코드

    @Column(name = "large_classification")
    private String largeClassification; // 대분류 이름

    @Column(name = "mid_classification")
    private String midClassification;   // 중분류 이름

    @Column(name = "small_classification")
    private String smallClassification; // 소분류 이름

    @Column(name = "theme")
    private String theme;   // 우리 서비스에서 지원하는 테마 5가지
    // 자연 속에서 힐링 / 문화예술 및 역사탐방 / 미식 여행 및 먹방 중심
    // / 열정적인 쇼핑투어 / 체험 및 액티비티 / 기타( 기타에는 분류하기애매한애들 또는 안되는애들 모아놨어요 )
}
*/
