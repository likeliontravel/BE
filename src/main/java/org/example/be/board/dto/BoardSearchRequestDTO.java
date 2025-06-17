package org.example.be.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.be.board.entity.BoardSortType;

@Getter
@AllArgsConstructor
public class BoardSearchRequestDTO {
    private String theme;   // 테마
    private String region;   // 지역
    private String searchKeyword;
    private BoardSortType boardSortType;
    private Integer page;
    private Integer size;
}
