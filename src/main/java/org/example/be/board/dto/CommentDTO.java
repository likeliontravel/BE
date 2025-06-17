package org.example.be.board.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CommentDTO {
    private Long id;
    private String commentWriter;
    private String commentWriterIdentifier; // 권한 확인용
    private String commentWriterProfileImageUrl;
    private String commentContent;
    private Long boardId;
    private Long parentCommentId; // 대댓글에 사용
    private LocalDateTime commentCreatedTime;

}
