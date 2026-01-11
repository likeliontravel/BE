package org.example.be.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.be.board.entity.BoardSortType;

@Getter
@AllArgsConstructor
public class SimplePageableRequestDTO {
    Integer page;
    Integer size;
    BoardSortType boardSortType;
}
