package org.example.be.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.be.board.entity.Board;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardDTO {
    private int id;
    private String title;
    private String content;
    private String writer;
    private int boardHits;
    private LocalDateTime createdTime;

    // 엔티티를 dio로 변환
    public static BoardDTO toDTO(Board board) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(board.getId());
        boardDTO.setTitle(board.getTitle());
        boardDTO.setContent(board.getContent());
        boardDTO.setWriter(board.getWriter());
        boardDTO.setBoardHits(board.getBoardHits());
        boardDTO.setCreatedTime(board.getCreatedTime());
        return boardDTO;
    }
}