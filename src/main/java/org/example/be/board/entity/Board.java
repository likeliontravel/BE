package org.example.be.board.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be.board.dto.BoardDTO;
import org.example.be.config.Base;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자
@Table(name = "board")
public class Board extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // PK

    // 제목
    @Column(nullable = false, length = 50)
    private String title;

    // 내용
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")  // TEXT(글자 수 54,535까지 저장)와 달리 MEDIUMTEXT는 16MB까지 저장 가능.
    private String content;

    // 작성자
    @Column(nullable = false)
    private String writer;

    // 조회수 인기글을 받아오기 위해 사용
    @Column(nullable = false)
    private int boardHits;

    // 테마 ( 필수 입력 )
    @Column(nullable = false)
    private String theme;

    // 지역 ( 필수 입력 )
    @Column(nullable = false)
    private String region;

    // 썸네일 이미지 퍼블릭URL
    @Column
    private String thumbnailPublicUrl;

    // 댓글 리스트
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Comment> commentList = new ArrayList<>();

    // dto를 엔티티로 변환 ( 게시글 생성 )
    public static Board toCreateEntity(BoardDTO boardDTO) {
        Board board = new Board();
        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setBoardHits(0); // 생성할 때는 조회수가 0
        board.setTheme(boardDTO.getTheme());
        board.setRegion(boardDTO.getRegion());
        board.setThumbnailPublicUrl(boardDTO.getThumbnailPublicUrl());
        return board;
    }

    // dto를 엔티티로 변환 ( 게시글 업데이트 )
    public static Board toUpdateEntity(BoardDTO boardDTO) {
        Board board = new Board();
        board.setId(boardDTO.getId());
        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setWriter(boardDTO.getWriter());
        board.setBoardHits(boardDTO.getBoardHits());
        board.setTheme(boardDTO.getTheme());
        board.setRegion(boardDTO.getRegion());
        board.setThumbnailPublicUrl(boardDTO.getThumbnailPublicUrl());
        return board;
    }
}
