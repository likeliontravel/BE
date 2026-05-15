package org.example.be.domain.board.controller;

import org.example.be.domain.board.dto.request.CommentCreateReqBody;
import org.example.be.domain.board.dto.request.CommentUpdateReqBody;
import org.example.be.domain.board.dto.response.CommentResBody;
import org.example.be.domain.board.service.CommentService;
import org.example.be.global.response.CommonResponse;
import org.example.be.global.response.PageResponse;
import org.example.be.global.security.config.SecurityUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
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
	public ResponseEntity<CommonResponse<PageResponse<CommentResBody>>> getAllComments(@PathVariable Long boardId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);

		PageResponse<CommentResBody> comments = commentService.getAllComments(boardId, pageable);
		return ResponseEntity.ok(CommonResponse.success(comments, "댓글 목록 조회 성공"));
	}

	//댓글 작성
	@PostMapping("/{boardId}")
	public ResponseEntity<CommonResponse<CommentResBody>> write(@PathVariable Long boardId,
		@Valid @RequestBody CommentCreateReqBody reqBody,
		@AuthenticationPrincipal SecurityUser user) {

		CommentResBody resBody = commentService.writeComment(boardId, reqBody, user.getId());
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(CommonResponse.success(resBody, "댓글이 작성되었습니다."));
	}

	// 댓글 수정 - 작성자 본인만 수정 가능
	@PutMapping("/{commentId}")
	public ResponseEntity<CommonResponse<CommentResBody>> update(@PathVariable Long commentId,
		@Valid @RequestBody CommentUpdateReqBody commentResBody,
		@AuthenticationPrincipal SecurityUser user) {

		CommentResBody resBody = commentService.updateComment(commentId, commentResBody, user.getId());
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(CommonResponse.success(resBody, "댓글이 수정되었습니다."));
	}

	// 댓글 삭제
	@DeleteMapping("/{commentId}")
	public ResponseEntity<CommonResponse<String>> delete(@PathVariable Long commentId,
		@AuthenticationPrincipal SecurityUser user) {

		commentService.deleteComment(commentId, user.getId());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "댓글 삭제 성공"));
	}

}
