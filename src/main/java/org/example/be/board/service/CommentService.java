package org.example.be.board.service;

import lombok.RequiredArgsConstructor;
import org.example.be.board.dto.CommentDTO;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.Comment;
import org.example.be.board.repository.BoardRepository;
import org.example.be.board.repository.CommentRepository;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.dto.UnifiedUsersNameAndProfileImageUrl;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UnifiedUserService unifiedUserService;
    private final MailProperties mailProperties;

    // 해당 게시글 댓글 조회
    public List<CommentDTO> getAllComments(Long boardId) {

        boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. boardId: " + boardId));

        List<Comment> commentList = commentRepository.findByBoardId(boardId);

        return commentList.stream().map(comment -> {
            CommentDTO dto = new CommentDTO();
            dto.setId(comment.getId());
            dto.setCommentContent(comment.getCommentContent());
            dto.setBoardId(boardId);
            dto.setCommentWriterIdentifier(comment.getCommentWriterIdentifier());
            dto.setCommentCreatedTime(comment.getCreatedTime());

            if (comment.getParentComment() != null) {
                dto.setParentCommentId(comment.getParentComment().getId());
            }

            // 작성자 프로필 정보 (이름, 프로필 사진) 별도 조회 후 dto set
            UnifiedUsersNameAndProfileImageUrl profile = unifiedUserService.getNameAndProfileImageUrlByUserIdentifier(comment.getCommentWriterIdentifier());
            dto.setCommentWriter(profile.getName());
            dto.setCommentWriterProfileImageUrl(profile.getProfileImageUrl());

            return dto;
        }).toList();

    }

    // 댓글 작성
    @Transactional
    public void writecomment(CommentDTO commentDTO) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        String writer = unifiedUserService.getNameByUserIdentifier(userIdentifier);

        Board boardEntity = boardRepository.findById(commentDTO.getBoardId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. boardId: " + commentDTO.getBoardId()));

        Comment comment;

        if (commentDTO.getParentCommentId() == null) {
            comment = new Comment();
        } else {
            Comment parent = commentRepository.findById(commentDTO.getParentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("이 자식 댓글의 부모 댓글이 존재하지 않습니다. "));
            comment = new Comment();
            comment.setParentComment(parent);
        }
        comment.setBoard(boardEntity);
        comment.setCommentWriter(writer);
        comment.setCommentWriterIdentifier(userIdentifier);
        comment.setCommentContent(commentDTO.getCommentContent());

        commentRepository.save(comment);
    }

    // 댓글 수정
    @Transactional
    public void updatecommemt(CommentDTO commentDTO) {
        String currentUserIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        Comment comment = commentRepository.findById(commentDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id: " + commentDTO.getId()));

        if (!currentUserIdentifier.equals(comment.getCommentWriterIdentifier())) {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }

        comment.setCommentContent(commentDTO.getCommentContent());
        commentRepository.save(comment);
    }

    @Transactional
    public void deletecomment(Long id) {
        String currentUserIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!currentUserIdentifier.equals(comment.getCommentWriterIdentifier())) {
            throw new IllegalArgumentException("작성자 본인만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
        commentRepository.flush();
    }
}
