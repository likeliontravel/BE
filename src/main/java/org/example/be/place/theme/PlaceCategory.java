package org.example.be.place.theme;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "place_category")
public class PlaceCategory {

    @Id
    @Column(name = "cat3")
    private String cat3;    // 소분류 코드를 PK로 사용. ** Long Id 아님! **

    @Column(name = "contentTypeId")
    private String contentTypeId;

    @Column(name = "cat1")
    private String cat1;

    @Column(name = "cat2")
    private String cat2;

    @Column(name = "cat3")
    private String cat3;

    @Column(name = "large_classification")
    private String largeClassification;

    @Column(name = "mid_classification")
    private String midClassification;

    @Column(name = "small_classification")
    private String smallClassification;

    @Column(name = "theme")
    private String theme;
}
