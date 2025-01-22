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

    public void write(CommentDTO commentDTO) {
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
}
