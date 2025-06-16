package org.example.be.place.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.be.place.entity.Place;

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

}
