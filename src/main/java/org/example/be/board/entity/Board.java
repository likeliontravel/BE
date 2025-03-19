package org.example.be.board.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.board.dto.BoardDTO;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor //기본 생성자
@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자
@Table(name = "board")
public class Board extends Base {
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

    @Column
    private int fileAttached; // 1이면 이미지 파일이 있는 것

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true,fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<BoardFile> boardFileList = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true,fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Comment> commentList = new ArrayList<>();


    // dto를 엔티티로 변환(게시글 생성)
    public static Board toSaveEntity(BoardDTO boardDTO) {
        Board board = new Board();
        board.setWriter(boardDTO.getWriter());
        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setBoardHits(0); // 생성할 때는 조회수가 0
        board.setFileAttached(0); // 파일이 없음.
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

    public static Board toSaveFileEntity(BoardDTO boardDTO) {
        Board board = new Board();
        board.setWriter(boardDTO.getWriter());
        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setBoardHits(0); // 생성할 때는 조회수가 0
        board.setFileAttached(1); // 파일이 있음.
        return board;
    }
}
