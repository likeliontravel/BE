package org.example.be.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.board.dto.BoardDTO;
import org.example.be.board.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    //전체 게시판 글 조회
    @GetMapping("/boards")
    public ResponseEntity<List<BoardDTO>> getBoards(
            @RequestParam(defaultValue = "0") int page,    // 기본값: 0
            @RequestParam(defaultValue = "10") int size) {  // 기본값: 10
        List<BoardDTO> boardDTOList = boardService.getAllBoards(page, size);
        return ResponseEntity.ok(boardDTOList);
    }

    // 인기순 정렬 게시판 글 조회 (조회수 기준)
    @GetMapping("/boards/popular")
    public ResponseEntity<List<BoardDTO>> getPopularBoards(
            @RequestParam(defaultValue = "0") int page,    // 기본값: 0
            @RequestParam(defaultValue = "10") int size) {  // 기본값: 10
        List<BoardDTO> popularBoards = boardService.getBoardsSortedByHits(page, size);
        return ResponseEntity.ok(popularBoards);
    }

    //게시글 등록
    @PostMapping("/write")
    public ResponseEntity<String> writeboard(@RequestBody BoardDTO boardDTO) {
        boardService.write(boardDTO);
        return ResponseEntity.ok("게시판이 작성되었습니다.");
    }


    //게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable int id, @RequestBody BoardDTO boardDTO) {
        boardService.updateBoard(boardDTO);
        return ResponseEntity.ok("게시판이 수정되었습니다.");
    }

    //게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable int id, @RequestBody BoardDTO boardDTO) {
        boardService.deleteBoard(boardDTO);
        return ResponseEntity.ok("게시판이 삭제 되었습니다.");
    }

    // 게시판 검색 (제목이나 내용으로 검색)
    @GetMapping("/search")
    public ResponseEntity<List<BoardDTO>> searchBoards(@RequestParam String keyword) {
        List<BoardDTO> searchBoardList = boardService.searchBoardsByKeyword(keyword);
        return ResponseEntity.ok(searchBoardList);
    }

}
