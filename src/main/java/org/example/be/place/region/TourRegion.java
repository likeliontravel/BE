package org.example.be.place.region;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "tour_region")
public class TourRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "siGunGuCode")
    private String siGunGuCode;    // Integer타입

    @Column(name = "SiGunGuName")
    private String siGunGuName;

    @Column(name = "areaCode")
    private String areaCode;

    @Column(name = "areaName")
    private String areaName;

    @Column(name = "regionKeyword")
    private String regionKeyword;   // 우리 서비스에서 지원하는 지역 ( 미리 나눠둔 지역 )
}
