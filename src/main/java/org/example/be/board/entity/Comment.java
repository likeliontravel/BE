package org.example.be.board.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.be.board.dto.CommentDTO;

@Entity
@Getter
@Setter
@Table(name = "comment")
public class Comment extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 20, nullable = false)
    private String commentWriter;

    @Column
    private String commentContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    public static Comment toSaveEntity(CommentDTO commentDTO,Board board) {
        Comment comment = new Comment();
        comment.setCommentWriter(commentDTO.getCommentWriter());
        comment.setCommentContent(commentDTO.getCommentContent());
        comment.setBoard(board);
        return comment;
    }
    public static Comment toUpdateEntity(CommentDTO commentDTO,Board board) {
        Comment comment = new Comment();
        comment.setCommentWriter(commentDTO.getCommentWriter());
        comment.setCommentContent(commentDTO.getCommentContent());
        comment.setId(commentDTO.getId());
        comment.setBoard(board);
        return comment;
    }
}
