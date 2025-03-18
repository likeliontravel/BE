package org.example.be.board.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CommentDTO {
    private int id;
    private String commentWriter;
    private String commentContent;
    private int boardId;
    private Integer parentCommentId; // 대댓글에 사용
    private LocalDateTime commentCreatedTime;

}
