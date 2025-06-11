package org.example.be.Tourapi.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.be.Tourapi.entity.PlaceSortType;

import java.util.List;

@Getter
@Setter
public class PlaceFilterRequestDTO {

    private List<String> regions;   // ex) ["강원", "강릉"]
    private List<String> themes;    // ex) ["체험 및 액티비티", "미식 여행 및 먹방 중심"]
    private String keyword;         // 검색어

    private int page = 1;
    private int size = 30;
    private PlaceSortType sortType = PlaceSortType.TITLE_ASC;   // 기본값 - 이름 오름차순
}
