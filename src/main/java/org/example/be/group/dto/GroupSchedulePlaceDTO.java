package org.example.be.group.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.be.place.entity.PlaceType;

import java.time.LocalDateTime;

// 방문 장소 ( 장소 블럭 세부정보 )
@Getter
@Setter
public class GroupSchedulePlaceDTO {
    private String contentId;
    private PlaceType placeType;    // 장소 타입 ( 관광지 / 숙소 / 식당 )
    private String title;
    private String address;
    private LocalDateTime visitStart;
    private LocalDateTime visitEnd;
    private int dayOrder;
    private int orderInDay;
}
