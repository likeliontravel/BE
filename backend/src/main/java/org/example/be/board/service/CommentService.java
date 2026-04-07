package org.example.be.board.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.example.be.board.dto.CommentCreateReqBody;
import org.example.be.board.dto.CommentResBody;
import org.example.be.board.dto.CommentUpdateReqBody;
import org.example.be.board.entity.Board;
import org.example.be.board.entity.Comment;
import org.example.be.board.repository.BoardRepository;
import org.example.be.board.repository.CommentRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.member.entity.Member;
import org.example.be.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
	@Transactional
	public Page<CommentResBody> getAllComments(Long boardId, Pageable pageable) {
		boardRepository.findById(boardId)
			.orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. boardId: " + boardId));

		Page<Comment> rootPage = commentRepository.findRootComments(boardId, pageable);
		List<Comment> rootComments = rootPage.getContent();

		if (rootComments.isEmpty())
			return new PageImpl<>(List.of(), pageable, 0);

		List<Long> rootIds = rootComments.stream().map(Comment::getId).toList();
		List<Comment> childComments = commentRepository.findChildrenByParentIds(rootIds);

		Map<Long, List<CommentResBody>> childMap = childComments.stream()
			.map(CommentResBody::from)
			.collect(Collectors.groupingBy(CommentResBody::parentCommentId));

		List<CommentResBody> result = rootComments.stream()
			.map(root -> {
				CommentResBody rootDto = CommentResBody.from(root);
				rootDto.childComments().addAll(childMap.getOrDefault(root.getId(), new ArrayList<>()));
				return rootDto;
			})
			.toList();

		return new PageImpl<>(result, pageable, rootPage.getTotalElements());
	}

	// 댓글 작성
	@Transactional
	public CommentResBody writeComment(Long boardId, CommentCreateReqBody reqBody, Long userId) {
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
				throw new BusinessException(ErrorCode.INVALID_PARENT_COMMENT);
			}
		}
		try {
			Comment comment = Comment.toCreateEntity(reqBody, writer, boardEntity, parentComment);
			return CommentResBody.from(commentRepository.save(comment));
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_CREATION_FAILED, "댓글 작성 실패 - message: " + e.getMessage());
		}
	}

	// 댓글 수정
	@Transactional
	public CommentResBody updateComment(Long commentId, CommentUpdateReqBody reqBody, Long userId) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("해당 댓글이 존재하지 않습니다. id: " + commentId));

		if (!comment.getWriter().getId().equals(userId)) {
			///  TODO: 1. BOARD_NOT_WRITER라는 이름의 예외로 댓글, 게시글 다 처리했음 | 2. userId로 사용했는데, 실제로 memberId가져오는건 똑같아서 로그에 memberId라는 이름으로 남게 했음 | 이 두가지 안내.
			throw new BusinessException(ErrorCode.BOARD_NOT_WRITER, "memberId: " + userId);
		}

		try {
			comment.toUpdateEntity(reqBody);
			return CommentResBody.from(comment);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_UPDATE_FAILED, "댓글 수정 실패 - message: " + e.getMessage());
		}
	}

	@Transactional
	public void deleteComment(Long commentId, Long userId) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다."));

		if (!comment.getWriter().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.BOARD_NOT_WRITER, "memberId: " + userId);
		}
		try {
			commentRepository.delete(comment);
			commentRepository.flush();
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_DELETE_FAILED, "댓글 삭제 실패 - message: " + e.getMessage());
		}
	}
}
