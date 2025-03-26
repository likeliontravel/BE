package org.example.be.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.BoardFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BoardDTO {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private int boardHits;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // 엔티티를 dto로 변환
    public static BoardDTO toDTO(Board board) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(board.getId());
        boardDTO.setTitle(board.getTitle());
        boardDTO.setContent(board.getContent());
        boardDTO.setWriter(board.getWriter());
        boardDTO.setBoardHits(board.getBoardHits());
        boardDTO.setCreatedTime(board.getCreatedTime());
        boardDTO.setUpdatedTime(board.getUpdatedTime());

        return boardDTO;
    }
}