package org.example.be.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.be.config.Base;

@Entity
@Getter
@Setter
@Table(name = "boardfile")

public class BoardFile extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String originalFileName;

    @Column
    private String storedFileName;

    @ManyToOne(fetch = FetchType.LAZY) // N 대 1 관계
    @JoinColumn(name = "board_id")
    private Board board;

    public static BoardFile toBoardFile(Board board, String originalFileName, String storedFileName) {
        BoardFile boardFile = new BoardFile();
        boardFile.setOriginalFileName(originalFileName);
        boardFile.setStoredFileName(storedFileName);
        boardFile.setBoard(board);
        return boardFile;
    }
}
