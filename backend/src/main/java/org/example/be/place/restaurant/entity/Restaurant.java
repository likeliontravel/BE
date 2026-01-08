package org.example.be.place.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.be.place.entity.Place;
import org.example.be.place.region.TourRegion;
import org.example.be.place.theme.PlaceCategory;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "restaurant")
public class Restaurant extends Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_region_id")
    private TourRegion tourRegion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_category_id")
    private PlaceCategory placeCategory;
}
