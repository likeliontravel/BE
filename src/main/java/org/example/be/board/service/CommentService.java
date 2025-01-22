package org.example.be.board.service;

import lombok.RequiredArgsConstructor;
import org.example.be.board.dto.CommentDTO;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.Comment;
import org.example.be.board.repository.BoardRepository;
import org.example.be.board.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    public void writecomment(CommentDTO commentDTO) {
        //부모엔티티를 먼저 조회
        Optional<Board> optionalBoardEntity = boardRepository.findById(commentDTO.getBoardId());
        if (optionalBoardEntity.isPresent()) {
            Board boardEntity = optionalBoardEntity.get();
            Comment comment = Comment.toSaveEntity(commentDTO, boardEntity);
            commentRepository.save(comment).getId();

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

        // 댓글을 업데이트하는 엔티티 생성 후 저장
        commentRepository.save(comment.toUpdateEntity(commentDTO, board));
    }


    public void deletecomment(int id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        commentRepository.delete(comment);
    }
}
