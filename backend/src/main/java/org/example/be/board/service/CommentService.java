package org.example.be.board.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.example.be.board.dto.CommentDTO;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.Comment;
import org.example.be.board.repository.BoardRepository;
import org.example.be.board.repository.CommentRepository;
import org.example.be.exception.custom.ForbiddenResourceAccessException;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.dto.UnifiedUsersNameAndProfileImageUrl;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

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
			.orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. boardId: " + boardId));

		List<Comment> commentList = commentRepository.findByBoardId(boardId);

		if (commentList.isEmpty()) {
			throw new NoSuchElementException("해당 게시글에 댓글이 존재하지 않습니다. boardId: " + boardId);
		}

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
			UnifiedUsersNameAndProfileImageUrl profile = unifiedUserService.getNameAndProfileImageUrlByUserIdentifier(
				comment.getCommentWriterIdentifier());
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
			.orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다. boardId: " + commentDTO.getBoardId()));

		Comment comment;

		if (commentDTO.getParentCommentId() == null) {
			comment = new Comment();
		} else {
			Comment parent = commentRepository.findById(commentDTO.getParentCommentId())
				.orElseThrow(() -> new NoSuchElementException("이 자식 댓글의 부모 댓글이 존재하지 않습니다. "));
			comment = new Comment();
			comment.setParentComment(parent);
		}
		comment.setBoard(boardEntity);
		comment.setCommentWriter(writer);
		comment.setCommentWriterIdentifier(userIdentifier);
		comment.setCommentContent(commentDTO.getCommentContent());

		try {
			commentRepository.save(comment);
		} catch (Exception e) {
			throw new ResourceCreationException("댓글 작성 실패", e);
		}
	}

	// 댓글 수정
	@Transactional
	public void updatecommemt(CommentDTO commentDTO) {
		String currentUserIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

		Comment comment = commentRepository.findById(commentDTO.getId())
			.orElseThrow(() -> new NoSuchElementException("해당 댓글이 존재하지 않습니다. id: " + commentDTO.getId()));

		if (!currentUserIdentifier.equals(comment.getCommentWriterIdentifier())) {
			throw new ForbiddenResourceAccessException("작성자 본인만 수정할 수 있습니다.");
		}

		comment.setCommentContent(commentDTO.getCommentContent());
		try {
			commentRepository.save(comment);
		} catch (Exception e) {
			throw new ResourceUpdateException("댓글 수정 실패", e);
		}
	}

	@Transactional
	public void deletecomment(Long id) {
		String currentUserIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

		Comment comment = commentRepository.findById(id)
			.orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다."));

		if (!currentUserIdentifier.equals(comment.getCommentWriterIdentifier())) {
			throw new ForbiddenResourceAccessException("작성자 본인만 삭제할 수 있습니다.");
		}
		try {
			commentRepository.delete(comment);
			commentRepository.flush();
		} catch (Exception e) {
			throw new ResourceDeletionException("댓글 삭제 실패", e);
		}
	}
}
