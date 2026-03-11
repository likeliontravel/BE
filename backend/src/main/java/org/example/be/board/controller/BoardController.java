package org.example.be.board.controller;

import java.util.List;

import org.example.be.board.dto.BoardResBody;
import org.example.be.board.dto.BoardSearchReqBody;
import org.example.be.board.dto.SimplePageableRequestDTO;
import org.example.be.board.entity.BoardSortType;
import org.example.be.board.service.BoardService;
import org.example.be.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	// 전체 게시판 글 목록 조회 ( 인기순 / 최신순 )
	// * param : page, size, BoardSortType( POPULAR / RECENT ) default:POPULAR
	// * return : 입력한 정렬타입으로 정렬한 전체 BoardDTO List
	@GetMapping("/all")
	public ResponseEntity<CommonResponse<List<BoardResBody>>> getAllBoard(
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer size,
		@RequestParam(required = false) BoardSortType boardSortType) {
		SimplePageableRequestDTO request = new SimplePageableRequestDTO(page, size, boardSortType);
		List<BoardResBody> allBoardList = boardService.getSortedBoardList(request);
		return ResponseEntity.ok(CommonResponse.success(allBoardList, "정렬된 전체 게시글 조회 성공"));
	}

	// 키워드 검색으로 게시판 글 목록 조회
	// * param : BoardSortType( POPULAR / RECENT ), searchKeyword
	// * return : 입력한 정렬타입으로 정렬한 키워드 검색 결과 BoardDTO List
	@GetMapping("/search")
	public ResponseEntity<CommonResponse<List<BoardResBody>>> getSearchedBoard(
		@RequestParam String searchKeyword,
		@RequestParam(required = false) BoardSortType boardSortType) {
		BoardSearchReqBody request = new BoardSearchReqBody(null, null, searchKeyword, boardSortType, null, null);
		List<BoardResBody> searchedBoardList = boardService.searchBoardByKeyword(request);
		return ResponseEntity.ok(CommonResponse.success(searchedBoardList, "게시판 키워드 검색 성공"));
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
		List<BoardResBody> boardResBodyList = boardService.searchBoardByTheme(request);
		return ResponseEntity.ok(CommonResponse.success(boardResBodyList, "테마 별 게시판 목록 조회 성공"));
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
		List<BoardResBody> boardResBodyList = boardService.searchBoardByRegion(request);
		return ResponseEntity.ok(CommonResponse.success(boardResBodyList, "지역 별 게시판 목록 조회 성공"));
	}

	// ==================== 게시판 글 관리 ( 생성 / 수정 / 삭제 ) ==================== //
	// 게시판 글 생성 ( 작성 )
	@PostMapping("/create")
	public ResponseEntity<CommonResponse<BoardResBody>> writeBoard(@RequestBody BoardResBody boardResBody) {
		BoardResBody savedBoardResBody = boardService.writeBoard(boardResBody);
		return ResponseEntity.ok(CommonResponse.success(savedBoardResBody, "게시글 저장 성공"));
	}

	// 게시판 글 수정
	@PutMapping("/update")
	public ResponseEntity<CommonResponse<BoardResBody>> updateBoard(@RequestBody BoardResBody boardResBody) {
		BoardResBody updatedBoardResBody = boardService.updateBoard(boardResBody);
		return ResponseEntity.ok(CommonResponse.success(updatedBoardResBody, "게시글 글 수정 성공"));
	}

	// 게시판 글 삭제
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<CommonResponse<Void>> deleteBoard(@PathVariable Long id) {
		boardService.deleteBoard(id);
		return ResponseEntity.ok(CommonResponse.success(null, "게시글 삭제 성공"));
	}
}
