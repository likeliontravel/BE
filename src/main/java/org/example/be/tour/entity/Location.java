package org.example.be.tour.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.board.entity.Base;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "location")
public class Location extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // 지역명 (예: 서울, 부산, 대전)

    @Column(nullable = false, unique = true)
    private int areaCode; // TourAPI에서 제공하는 지역 코드
}
