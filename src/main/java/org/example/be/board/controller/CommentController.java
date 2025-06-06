package org.example.be.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.board.dto.CommentDTO;
import org.example.be.board.service.CommentService;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    // 댓글 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<CommonResponse<List<CommentDTO>>> getAllComments(@PathVariable Long boardId) {
        List<CommentDTO> comments = commentService.getAllComments(boardId);
        return ResponseEntity.ok(CommonResponse.success(comments, "댓글 목록 조회 성공"));
    }

    //댓글 작성
    @PostMapping
    public ResponseEntity<CommonResponse<String>> write(@RequestBody CommentDTO commentDTO) {
        // 댓글 작성 로직 수행
        commentService.writecomment(commentDTO);
        // 성공적인 댓글 작성 후 ResponseEntity로 응답
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("댓글이 작성되었습니다.", "댓글 등록 성공"));
    }

    // 댓글 수정 - 작성자 본인만 수정 가능
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> update(@PathVariable Long id, @RequestBody CommentDTO commentDTO) {
        commentDTO.setId(id);
        commentService.updatecommemt(commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("댓글이 수정되었습니다.", "댓글 수정 성공"));
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> delete(@PathVariable Long id) {
        commentService.deletecomment(id);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "댓글 삭제 성공"));
    }

}
