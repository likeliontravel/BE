package org.example.be.place.entity;

public interface Place {
    String getName();
    String getAddress();
    String getContact();    // 연락처
    String getSiGunGuCode();    // 시군구 코드 (지역)
    String getCat3();      // 해당 장소 소분류 카테고리
    String getTheme();     // 테마
    String getRegion();
}
