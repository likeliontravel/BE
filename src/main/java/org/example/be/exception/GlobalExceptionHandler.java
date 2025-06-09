package org.example.be.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.be.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("[잘못된 요청 파라미터] {}", e.getMessage());
        return ResponseEntity.badRequest().body(CommonResponse.error(400, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<CommonResponse<Void>> handleAllOtherException(Exception e) {
        log.error("[그 외 오류] {}", e.getMessage());
        return ResponseEntity.badRequest().body(CommonResponse.error(500, e.getMessage()));
    }
}
