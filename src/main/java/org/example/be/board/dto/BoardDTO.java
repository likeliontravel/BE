package org.example.be.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.be.board.entity.Board;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardDTO {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private int boardHits;
    private String theme;
    private String region;
    private String thumbnailPublicUrl;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String writerIdentifier; // 작성자 본인확인 로직 강화 - 0520 추가
    private String writerProfileImageUrl;


    // 엔티티를 dto로 변환
    public static BoardDTO toDTO(Board board) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(board.getId());
        boardDTO.setTitle(board.getTitle());
        boardDTO.setContent(board.getContent());
        boardDTO.setWriter(board.getWriter());
        boardDTO.setBoardHits(board.getBoardHits());
        boardDTO.setTheme(board.getTheme());
        boardDTO.setRegion(board.getRegion());
        boardDTO.setWriterIdentifier(board.getWriterIdentifier());
        boardDTO.setThumbnailPublicUrl(board.getThumbnailPublicUrl());
        boardDTO.setCreatedTime(board.getCreatedTime());
        boardDTO.setUpdatedTime(board.getUpdatedTime());
        return boardDTO;
    }
}
