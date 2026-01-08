package org.example.be.place.touristSpot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.be.place.entity.Place;
import org.example.be.place.region.TourRegion;
import org.example.be.place.theme.PlaceCategory;

@Entity
@Table(name = "tourist_spot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TouristSpot extends Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_region_id")
    private TourRegion tourRegion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_category_id")
    private PlaceCategory placeCategory;

    // Place 클래스의 모든 필드 상속
}
