package org.example.test.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accommodation")
public class Accommodation extends BaseEntity {

    private String name;
    private String address;
    private String category;  // 호텔, 모텔, 펜션 등
    private String imageUrl;

    @Column(unique = true)
    private String contentId;   // TourAPI에서 제공하는 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")  // 'area_id' -> 'location_id'
    private Location location;  // 'area' -> 'location'
}