package org.example.be.board.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.be.board.dto.CommentDTO;
import org.example.be.config.Base;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "comment")
public class Comment extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String commentWriter;

    @Column
    private String commentWriterIdentifier; // 권한 확인용

    @Column
    private String commentContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentcomment_id")
    private Comment parentComment; // null이면 일반 댓글 아니면 대댓글

    @OneToMany(mappedBy = "parentComment",orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Comment> childComments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    // 부모댓글 작성시
    public static Comment toSaveEntity(CommentDTO commentDTO,Board board) {
        Comment comment = new Comment();
        comment.setCommentWriter(commentDTO.getCommentWriter());
        comment.setCommentContent(commentDTO.getCommentContent());
        comment.setCommentWriterIdentifier(commentDTO.getCommentWriterIdentifier());
        comment.setBoard(board);
        return comment;
    }

    // 대댓글인 경우
    public static Comment toSaveReplyEntity(CommentDTO commentDTO, Board board, Comment parentComment) {
        Comment comment = new Comment();
        comment.setCommentWriter(commentDTO.getCommentWriter());
        comment.setCommentWriterIdentifier(commentDTO.getCommentWriterIdentifier());
        comment.setCommentContent(commentDTO.getCommentContent());
        comment.setBoard(board);
        comment.setParentComment(parentComment);
        return comment;
    }

}
