package org.example.be.tour.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_region")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "areaCode")
    private String areaCode;

    @Column(name = "areaName")
    private String areaName;

    @Column(name = "siGunGuCode")
    private String siGunGuCode;

    @Column(name = "siGunGuName")
    private String siGunGuName;

    @Column(name = "region")
    private String region;
}
