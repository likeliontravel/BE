package org.example.be.Tourapi.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
                .body(CommonResponse.fail(errorMsg));
    }

    // 그 외 모든 예외 처리 (500 에러)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleAllException(Exception ex) {
        ex.printStackTrace(); // 서버 로그에 에러 출력
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fail("서버 내부 오류가 발생했습니다."));
    }
}