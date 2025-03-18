package org.example.be.board.service;

import lombok.RequiredArgsConstructor;
import org.example.be.board.dto.CommentDTO;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.Comment;
import org.example.be.board.repository.BoardRepository;
import org.example.be.board.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    public void writecomment(CommentDTO commentDTO) {
        //부모엔티티를 먼저 조회 후 일반 댓글 작성
        Optional<Board> optionalBoardEntity = boardRepository.findById(commentDTO.getBoardId());
        if (optionalBoardEntity.isPresent()) {
            Board boardEntity = optionalBoardEntity.get();

            //일반 댓글 생성(부모 댓글)
            if (commentDTO.getParentCommentId() == null) {
                Comment comment = Comment.toSaveEntity(commentDTO, boardEntity);
                commentRepository.save(comment).getId();
            } else {
                Comment parentComment = commentRepository.findById(commentDTO.getParentCommentId()).orElseThrow(() -> new RuntimeException("부모 댓글이 존재 하지 않습니다."));
                Comment comment = Comment.toSaveReplyEntity(commentDTO,boardEntity,parentComment);
                commentRepository.save(comment).getId();
            }

        } else {
            throw new RuntimeException("해당 게시글이 존재하지 않습니다.");
        }
    }

    public void updatecommemt(CommentDTO commentDTO) {
        // 댓글을 ID로 찾음
        Comment comment = commentRepository.findById(commentDTO.getId())
                .orElseThrow(() -> new RuntimeException("해당 댓글이 존재하지 않습니다."));

        // BoardRepository를 사용하여 Board를 조회
        Board board = boardRepository.findById(commentDTO.getBoardId())
                .orElseThrow(() -> new RuntimeException("해당 게시글이 존재하지 않습니다."));

        // 대댓글인지 일반 댓글인지를 확인
        if (comment.getParentComment() != null) {
            // 대댓글 수정 시 부모 댓글을 변경하지 않고, 대댓글만 수정
            comment.setCommentContent(commentDTO.getCommentContent()); // 대댓글 수정 내용
            comment.setCommentWriter(commentDTO.getCommentWriter()); // 대댓글 작성자 수정 내용
            comment.setBoard(board); // 댓글이 속한 게시글 설정
            commentRepository.save(comment); // 대댓글 저장
        } else {
            // 일반 댓글 수정
            comment.setCommentContent(commentDTO.getCommentContent()); // 일반 댓글 수정 내용
            comment.setCommentWriter(commentDTO.getCommentWriter()); // 일반 댓글 작성자 수정 내용
            comment.setBoard(board); // 댓글이 속한 게시글 설정
            commentRepository.save(comment); // 일반 댓글 저장
        }
    }

    @Transactional
    public void deletecomment(int id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        commentRepository.delete(comment);
        commentRepository.flush();
    }
}
