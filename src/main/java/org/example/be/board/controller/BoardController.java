package org.example.be.board.controller;

import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import org.example.be.board.dto.BoardDTO;
import org.example.be.board.service.BoardService;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    // 전체 게시판 글 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<BoardDTO>>> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        List<BoardDTO> boardDTOList = boardService.getAllBoards(page, size);
        return ResponseEntity.ok(CommonResponse.success(boardDTOList, "게시판 글 조회 성공"));
    }
    //게시판 글 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<BoardDTO>> getBoardById(@PathVariable int id) {
        BoardDTO boardDTO = boardService.getBoard(id);
        return ResponseEntity.ok(CommonResponse.success(boardDTO, "게시글 조회 성공"));
    }

    // 인기순 게시판 글 조회 (조회수 기준)
    @GetMapping("/popular")
    public ResponseEntity<CommonResponse<List<BoardDTO>>> getPopularBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        List<BoardDTO> popularBoards = boardService.getBoardsSortedByHits(page, size);
        return ResponseEntity.ok(CommonResponse.success(popularBoards, "인기순 게시판 글 조회 성공"));
    }
    // 최신 게시판 글 조회
    @GetMapping("/recent")
    public ResponseEntity<CommonResponse<List<BoardDTO>>> getRecentBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ){
        List<BoardDTO> recentBoards = boardService.getBoardSortedByRecents(page, size);
        return ResponseEntity.ok(CommonResponse.success(recentBoards, "최신순 게시판 글 조회 성공"));
    }

    // 게시글 작성
    @PostMapping
    public ResponseEntity<CommonResponse<String>> writeBoard(@ModelAttribute BoardDTO boardDTO) throws IOException {
        boardService.write(boardDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("게시판이 작성되었습니다.", "게시글 등록 성공"));
    }


    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> update(@PathVariable int id,@RequestBody BoardDTO boardDTO) throws IOException {
        boardDTO.setId(id);
        boardService.updateBoard(boardDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("게시판이 수정되었습니다.", "게시글 수정 성공"));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> delete(@PathVariable int id) {
        boardService.deleteBoard(id);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null,"게시글 삭제"));
    }

    // 게시판 검색 (제목이나 내용으로 검색)
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<BoardDTO>>> searchBoards(@RequestParam String keyword) {
        List<BoardDTO> searchBoardList = boardService.searchBoardsByKeyword(keyword);
        return ResponseEntity.ok(CommonResponse.success(searchBoardList, "게시판 검색 성공"));
    }
}