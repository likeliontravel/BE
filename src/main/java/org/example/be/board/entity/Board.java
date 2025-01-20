package org.example.be.board.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.board.dto.BoardDTO;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor //기본 생성자
@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자
@Table(name = "board")
public class Board extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    //제목
    @Column(nullable = false, length = 50)
    private String title;
    //내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    //작성자
    @Column(nullable = false)
    private String writer;

    //조회수 인기글을 받아오기 위해 사용
    @Column(nullable = false)
    private int boardHits;


    // dto를 엔티티로 변환(게시글 생성)
    public static Board toSavaEntity(BoardDTO boardDTO) {
        Board board = new Board();
        board.setWriter(boardDTO.getWriter());
        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setBoardHits(0);
        return board;
    }
    public static Board toUpdateEntity(BoardDTO boardDTO) {
        Board board = new Board();
        board.setId(boardDTO.getId());
        board.setWriter(boardDTO.getWriter());
        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setBoardHits(boardDTO.getBoardHits());
        return board;
    }

}
