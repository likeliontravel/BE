package org.example.be.tour.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.board.entity.Base;

import java.awt.geom.Area;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "restaurant")
public class Restaurant extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    private String category;  // 한식, 양식, 중식 등
    private String imageUrl;

    @Column(unique = true)
    private Long contentId;   // TourAPI에서 제공하는 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")  // 'area_id' -> 'location_id'
    private Location location;  // 'area' -> 'location'
}
