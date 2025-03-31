package org.example.be.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.be.board.entity.SortType;

@Getter
@AllArgsConstructor
public class BoardSearchRequestDTO {
    private String theme;   // 테마
    private String region;   // 지역
    private String searchKeyword;
    private SortType sortType;
    private Integer page;
    private Integer size;
}
