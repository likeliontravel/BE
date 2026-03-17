package org.example.be.board.controller;

import java.util.List;

import org.example.be.board.dto.CommentCreateReqBody;
import org.example.be.board.dto.CommentResBody;
import org.example.be.board.dto.CommentUpdateReqBody;
import org.example.be.board.service.CommentService;
import org.example.be.response.CommonResponse;
import org.example.be.security.config.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

	private final CommentService commentService;

	// 댓글 조회
	@GetMapping("/{boardId}")
	public ResponseEntity<CommonResponse<List<CommentResBody>>> getAllComments(@PathVariable Long boardId) {
		List<CommentResBody> comments = commentService.getAllComments(boardId);
		return ResponseEntity.ok(CommonResponse.success(comments, "댓글 목록 조회 성공"));
	}

	//댓글 작성
	@PostMapping("{boardId}")
	public ResponseEntity<CommonResponse<String>> write(@PathVariable Long boardId,
		@Valid @RequestBody CommentCreateReqBody reqBody,
		@AuthenticationPrincipal SecurityUser user) {

		commentService.writeComment(boardId, reqBody, user.getId());
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(CommonResponse.success("댓글이 작성되었습니다.", "댓글 등록 성공"));
	}

	// 댓글 수정 - 작성자 본인만 수정 가능
	@PutMapping("/{commentId}")
	public ResponseEntity<CommonResponse<String>> update(@PathVariable Long commentId,
		@Valid @RequestBody CommentUpdateReqBody commentResBody,
		@AuthenticationPrincipal SecurityUser user) {

		commentService.updateComment(commentId, commentResBody, user.getId());
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(CommonResponse.success("댓글이 수정되었습니다.", "댓글 수정 성공"));
	}

	// 댓글 삭제
	@DeleteMapping("/{commentId}")
	public ResponseEntity<CommonResponse<String>> delete(@PathVariable Long commentId,
		@AuthenticationPrincipal SecurityUser user) {

		commentService.deleteComment(commentId, user.getId());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "댓글 삭제 성공"));
	}

}
