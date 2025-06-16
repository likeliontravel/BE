package org.example.be.place.touristSpot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.be.place.entity.Place;

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

    // Place 클래스의 모든 필드 상속
}
