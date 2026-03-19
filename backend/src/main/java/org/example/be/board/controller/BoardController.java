package org.example.be.board.controller;

import java.util.List;

import org.example.be.board.dto.BoardCreateReqBody;
import org.example.be.board.dto.BoardResBody;
import org.example.be.board.dto.BoardSearchReqBody;
import org.example.be.board.dto.BoardUpdateReqBody;
import org.example.be.board.dto.SimplePageableReqBody;
import org.example.be.board.entity.BoardSortType;
import org.example.be.board.service.BoardService;
import org.example.be.response.CommonResponse;
import org.example.be.security.config.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

	private final BoardService boardService;

	// ==================== 게시판 1개 조회 ==================== //
	// id로 게시판 글 1개 조회
	@GetMapping("/{id}")
	public ResponseEntity<CommonResponse<BoardResBody>> getBoard(@PathVariable Long id) {
		BoardResBody boardResBody = boardService.getBoard(id);
		return ResponseEntity.ok(CommonResponse.success(boardResBody, "게시판 글 조회 성공"));
	}

	// ==================== 게시판 목록 조회 ==================== //
	@GetMapping
	public ResponseEntity<CommonResponse<List<BoardResBody>>> getAllBoard(SimplePageableReqBody reqBody) {
		return ResponseEntity.ok(CommonResponse.success(boardService.getSortedBoardList(reqBody), "정렬된 전체 게시글 조회 성공"));
	}

	// 키워드 검색으로 게시판 글 목록 조회
	// * param : BoardSortType( POPULAR / RECENT ), searchKeyword
	// * return : 입력한 정렬타입으로 정렬한 키워드 검색 결과 BoardDTO List
	@GetMapping("/search")
	public ResponseEntity<CommonResponse<List<BoardResBody>>> getSearchedBoard(
		@RequestParam String searchKeyword,
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer size,
		@RequestParam(required = false) BoardSortType boardSortType) {

		BoardSearchReqBody request = new BoardSearchReqBody(null, null, searchKeyword, boardSortType, page, size);
		return ResponseEntity.ok(CommonResponse.success(boardService.searchBoard(request), "게시판 키워드 검색 성공"));
	}

	// 테마 별 게시판 글 목록 조회
	// * param : BoardSortType( POPULAR / RECENT ), theme
	// * return : 입력한 정렬타입으로 정렬한 해당 테마 검색 결과 BoardDTO List
	@GetMapping("/byTheme")
	public ResponseEntity<CommonResponse<List<BoardResBody>>> getBoardListByTheme(
		@RequestParam String theme,
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer size,
		@RequestParam(required = false) BoardSortType boardSortType) {
		BoardSearchReqBody request = new BoardSearchReqBody(theme, null, null, boardSortType, page, size);
		return ResponseEntity.ok(CommonResponse.success(boardService.searchBoard(request), "테마 별 게시판 목록 조회 성공"));
	}

	// 지역 별 게시판 글 목록 조회
	// * param : BoardSortType( POPULAR / RECENT ), region
	// * return : 입력한 정렬타입으로 정렬한 해당 지역 검색 결과 BoardDTO List
	@GetMapping("/byRegion")
	public ResponseEntity<CommonResponse<List<BoardResBody>>> getBoardListByRegion(
		@RequestParam String region,
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer size,
		@RequestParam(required = false) BoardSortType boardSortType) {
		BoardSearchReqBody request = new BoardSearchReqBody(null, region, null, boardSortType, page, size);
		return ResponseEntity.ok(CommonResponse.success(boardService.searchBoard(request), "지역 별 게시판 목록 조회 성공"));
	}

	// ==================== 게시판 글 관리 ( 생성 / 수정 / 삭제 ) ==================== //
	// 게시판 글 생성 ( 작성 )
	@PostMapping
	public ResponseEntity<CommonResponse<BoardResBody>> writeBoard(@Valid @RequestBody BoardCreateReqBody reqBody,
		@AuthenticationPrincipal SecurityUser user) {
		BoardResBody savedBoardResBody = boardService.writeBoard(reqBody, user.getId());
		return ResponseEntity.ok(CommonResponse.success(savedBoardResBody, "게시글 저장 성공"));
	}

	// 게시판 글 수정
	@PatchMapping("/{id}")
	public ResponseEntity<CommonResponse<BoardResBody>> updateBoard(@PathVariable Long id,
		@RequestBody BoardUpdateReqBody reqBody,
		@AuthenticationPrincipal SecurityUser user) {
		BoardResBody updatedBoardResBody = boardService.updateBoard(id, reqBody, user.getId());
		return ResponseEntity.ok(CommonResponse.success(updatedBoardResBody, "게시글 글 수정 성공"));
	}

	// 게시판 글 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<CommonResponse<Void>> deleteBoard(@PathVariable Long id,
		@AuthenticationPrincipal SecurityUser user) {
		boardService.deleteBoard(id, user.getId());
		return ResponseEntity.ok(CommonResponse.success(null, "게시글 삭제 성공"));
	}
}
