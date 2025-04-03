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
@Table(name = "accommodation")
public class Accommodation extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private String category;  // 호텔, 모텔, 펜션 등
    private String imageUrl; // 이미지 URL (선택)
    @Column(nullable = false, unique = true)
    private String contentId;
    @ManyToOne(fetch = FetchType.LAZY) // N:1 관계
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;   // 'area' -> 'location'
}
