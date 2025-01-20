package org.example.be.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.board.dto.BoardDTO;
import org.example.be.board.service.BoardService;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    // 전체 게시판 글 조회
    @GetMapping("/boards")
    public ResponseEntity<CommonResponse<List<BoardDTO>>> getBoards(
            @RequestParam(defaultValue = "0") int page,    // 기본값: 0
            @RequestParam(defaultValue = "10") int size) {  // 기본값: 10
        List<BoardDTO> boardDTOList = boardService.getAllBoards(page, size);
        return ResponseEntity.ok(CommonResponse.success(boardDTOList, "게시판 글 조회 성공"));
    }

    // 인기순 정렬 게시판 글 조회 (조회수 기준)
    @GetMapping("/boards/popular")
    public ResponseEntity<CommonResponse<List<BoardDTO>>> getPopularBoards(
            @RequestParam(defaultValue = "0") int page,    // 기본값: 0
            @RequestParam(defaultValue = "10") int size) {  // 기본값: 10
        List<BoardDTO> popularBoards = boardService.getBoardsSortedByHits(page, size);
        return ResponseEntity.ok(CommonResponse.success(popularBoards, "인기순 게시판 글 조회 성공"));
    }

    // 게시글 등록
    @PostMapping("/write")
    public ResponseEntity<CommonResponse<String>> writeBoard(@RequestBody BoardDTO boardDTO) {
        boardService.write(boardDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("게시판이 작성되었습니다.", "게시글 등록 성공"));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> update(@RequestBody BoardDTO boardDTO) {
        boardService.updateBoard(boardDTO);
        return ResponseEntity.ok(CommonResponse.success("게시판이 수정되었습니다.", "게시글 수정 성공"));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> delete(@PathVariable int id) {
        boardService.deleteBoard(id);  // id만 전달하여 삭제
        return ResponseEntity.ok(CommonResponse.success("게시판이 삭제되었습니다.", "게시글 삭제 성공"));
    }

    // 게시판 검색 (제목이나 내용으로 검색)
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<BoardDTO>>> searchBoards(@RequestParam String keyword) {
        List<BoardDTO> searchBoardList = boardService.searchBoardsByKeyword(keyword);
        return ResponseEntity.ok(CommonResponse.success(searchBoardList, "게시판 검색 성공"));
    }
}
