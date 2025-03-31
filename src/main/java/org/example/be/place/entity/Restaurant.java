package org.example.be.place.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.be.config.Base;

// 식당 엔티티
@Entity
@Getter
@Table(name = "restaurant")
public class Restaurant extends Base implements Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "contact")
    private String contact; // 연락처

    @Column(name = "siGunGuCode")
    private String siGunGuCode;

    @Column(name = "theme")
    private String theme;

    @Column(name = "cat3")
    private String cat3;

    @Column(name = "region")
    private String region;
}
