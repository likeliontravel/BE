package org.example.be.board.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.example.be.board.dto.CommentCreateReqBody;
import org.example.be.board.dto.CommentResBody;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.Comment;
import org.example.be.board.repository.BoardRepository;
import org.example.be.board.repository.CommentRepository;
import org.example.be.exception.custom.ForbiddenResourceAccessException;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.dto.UnifiedUsersNameAndProfileImageUrl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final BoardRepository boardRepository;
	private final MemberRepository memberRepository;

	// 해당 게시글 댓글 조회
	public List<CommentResBody> getAllComments(Long boardId) {

		boardRepository.findById(boardId)
			.orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. boardId: " + boardId));

		List<Comment> commentList = commentRepository.findByBoardId(boardId);

		if (commentList.isEmpty()) {
			throw new NoSuchElementException("해당 게시글에 댓글이 존재하지 않습니다. boardId: " + boardId);
		}

		return commentList.stream().map(comment -> {
			CommentResBody dto = new CommentResBody();
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
	public CommentResBody writecomment(Long boardId, CommentCreateReqBody reqBody, Long userId) {
		Member writer = memberRepository.findById(userId)
			.orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. userId: " + userId));

		Board boardEntity = boardRepository.findById(boardId)
			.orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다. boardId: " + boardId));

		Comment parentComment = null;

		if (reqBody.parentCommentId() != null) {
			parentComment = commentRepository.findById(reqBody.parentCommentId())
				.orElseThrow(() -> new NoSuchElementException(
					"이 자식 댓글의 부모 댓글이 존재하지 않습니다. parentCommentId: " + reqBody.parentCommentId()));
			
			if (!parentComment.getBoard().getId().equals(boardEntity.getId())) {
				throw new IllegalArgumentException("다른 게시글의 댓글에 대댓글을 달 수 없습니다.");
			}
		}
		Comment comment = Comment.toCreateEntity(reqBody.content(), writer, boardEntity, parentComment);

		try {
			Comment saveComment = commentRepository.save(comment);
			return CommentResBody.from(saveComment);
		} catch (Exception e) {
			throw new ResourceCreationException("댓글 작성 실패", e);
		}
	}

	// 댓글 수정
	@Transactional
	public void updatecommemt(CommentResBody commentResBody) {
		String currentUserIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

		Comment comment = commentRepository.findById(commentResBody.getId())
			.orElseThrow(() -> new NoSuchElementException("해당 댓글이 존재하지 않습니다. id: " + commentResBody.getId()));

		if (!currentUserIdentifier.equals(comment.getCommentWriterIdentifier())) {
			throw new ForbiddenResourceAccessException("작성자 본인만 수정할 수 있습니다.");
		}

		comment.setCommentContent(commentResBody.getCommentContent());
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
