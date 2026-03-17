package org.example.be.board.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.example.be.board.dto.CommentCreateReqBody;
import org.example.be.board.dto.CommentResBody;
import org.example.be.board.dto.CommentUpdateReqBody;
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

		List<Comment> allComments = commentRepository.findAllByBoardIdWithMember(boardId);

		List<CommentResBody> commentResBodyList = new ArrayList<>();
		Map<Long, CommentResBody> map = new HashMap<>();

		allComments.forEach(comment -> {
			CommentResBody dto = CommentResBody.from(comment);
			map.put(comment.getId(), dto);

			if (comment.getParentComment() != null) {
				CommentResBody parentDto = map.get(comment.getParentComment().getId());
				if (parentDto != null) {
					parentDto.childComments().add(dto);
				} else {
					commentResBodyList.add(dto);
					// 부모 댓글이 아직 map에 없으면 일단 최상위로 추가, 나중에 자식 댓글이 부모 댓글을 찾을 때 map에서 찾아서 자식 댓글로 추가
				}
			}
		});
		return commentResBodyList;
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
				throw new IllegalArgumentException("다른 게시글의 댓글에 대댓글을 달 수 없습니다.");
			}
		}
		try {
			Comment comment = Comment.toCreateEntity(reqBody, writer, boardEntity, parentComment);
			return CommentResBody.from(commentRepository.save(comment));
		} catch (Exception e) {
			throw new ResourceCreationException("댓글 작성 실패", e);
		}
	}

	// 댓글 수정
	@Transactional
	public CommentResBody updateComment(Long commentId, CommentUpdateReqBody reqBody, Long userId) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("해당 댓글이 존재하지 않습니다. id: " + commentId));

		if (!comment.getWriter().getId().equals(userId)) {
			throw new ForbiddenResourceAccessException("작성자 본인만 수정할 수 있습니다.");
		}

		try {
			comment.toUpdateEntity(reqBody);
			return CommentResBody.from(comment);
		} catch (Exception e) {
			throw new ResourceUpdateException("댓글 수정 실패", e);
		}
	}

	@Transactional
	public void deleteComment(Long commentId, Long userId) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다."));

		if (!comment.getWriter().getId().equals(userId)) {
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
