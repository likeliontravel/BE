package org.example.be.domain.board.exception;

import org.example.be.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 게시판 도메인 에러 코드.
 * 게시글, 댓글의 조회와 작성자 권한, 게시글 입력값과 이미지 업로드
 */
@Getter
@RequiredArgsConstructor
public enum BoardErrorCode implements ErrorCode {

	// --- 게시판 (Board) ---
	BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
	BOARD_NOT_WRITER(HttpStatus.FORBIDDEN, "게시글의 작성자만 접근할 수 있습니다."),
	COMMENT_NOT_WRITER(HttpStatus.FORBIDDEN, "댓글의 작성자만 접근할 수 있습니다."),
	INVALID_PARENT_COMMENT_OF_BOARD(HttpStatus.BAD_REQUEST, "다른 게시물의 댓글에는 대댓글을 달 수 없습니다."),
	INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "부모 댓글을 찾을 수 없습니다."),
	BOARD_TITLE_BLANK(HttpStatus.BAD_REQUEST, "게시글 제목은 비어있을 수 없습니다."),
	BOARD_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "게시글 내용은 비어있을 수 없습니다."),
	BOARD_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "게시글 이미지 업로드 가능 개수를 초과했습니다."),
	BOARD_IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 이미지가 없습니다.");

	private final HttpStatus status;
	private final String message;

}
