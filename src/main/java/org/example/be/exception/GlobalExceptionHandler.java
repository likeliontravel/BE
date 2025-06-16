package org.example.be.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.be.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    // 유효성 검사 실패 시 잡아서 처리
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<CommonResponse<Void>> handleValidationException(Exception ex) {
        String errorMsg = "요청 파라미터가 유효하지 않습니다.";
        if (ex instanceof BindException bindEx && !bindEx.getAllErrors().isEmpty()) {
            errorMsg = bindEx.getAllErrors().get(0).getDefaultMessage();
        } else if (ex instanceof MethodArgumentNotValidException validEx && !validEx.getBindingResult().getAllErrors().isEmpty()) {
            errorMsg = validEx.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }

        return ResponseEntity.badRequest()
                .body(CommonResponse.error(500, errorMsg));
    }

}
