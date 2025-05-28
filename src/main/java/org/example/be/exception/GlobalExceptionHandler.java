package org.example.be.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.be.exception.custom.ForbiddenResourceAccessException;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

// 전역 예외처리 핸들러
// 메시지는 서비스레이어에서 던지는대로 응답까지 전달
// 예외처리 아키텍쳐 : 기본 RestControllerAdvice사용, 일반@Controller로 잡혀도 JSON응답하도록 메서드에 @ResponseBody 붙이기
//  @MessageMapping붙은 웹소켓 예외는 감지 안되니 추후 다시 생각할 예정
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // IllegalArgumentException - 잘못된 인수가 들어온 경우
    // 응답 코드 : BadRequest 400
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[잘못된 인수 요청] {}", e.getMessage());
        return ResponseEntity.badRequest().body(CommonResponse.error(400, e.getMessage()));
    }

    // NoSuchElementException - 찾을 수 없음
    // 응답 코드 : NotFound 404
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseBody
    public ResponseEntity<CommonResponse<Void>> handleNoSuchElementException(NoSuchElementException e) {
        log.warn("[요청 관련 리소스 찾을 수 없음] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponse.error(404, e.getMessage()));
    }

    // Exception - 서버 내부 오류
    // 응답 코드 : InternalServerError 500
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("[서버 내부 오류] {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.error(500, e.getMessage()));
    }

    // ResourceCreationException - 자원 생성 오류 (또는 생성 후 정상 저장 실패 오류)
    // 응답 코드 : Inernal Server Error 500
    @ExceptionHandler(ResourceCreationException.class)
    @ResponseBody
    public ResponseEntity<CommonResponse<Void>> handleResourceCreationException(ResourceCreationException e) {
        log.error("[자원 생성 오류] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.error(500, e.getMessage()));
    }

    // ResourceUpdateException - 자원 수정 오류 (또는 수정 후 정상 저장 실패 오류)
    // 응답 코드 : Internal Server Error 500
    @ExceptionHandler(ResourceUpdateException.class)
    @ResponseBody
    public ResponseEntity<CommonResponse<Void>> handleResourceUpdateException(ResourceUpdateException e) {
        log.error("[자원 수정 오류] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.error(500, e.getMessage()));
    }

    // ResourceDeletionException - 자원 삭제 오류 (또는 삭제 후 정상 flush 도중 오류)
    // 응답 코드 : Internal Server Error 500
    @ExceptionHandler(ResourceDeletionException.class)
    @ResponseBody
    public ResponseEntity<CommonResponse<Void>> handleResourceDeletionException(ResourceDeletionException e) {
        log.error("[자원 삭제 오류] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.error(500, e.getMessage()));
    }

    // ForbiddenResourceAccessException - 인증은 성공했으나 권한이 없어 인가할 수 없는 경우
    // 응답 코드 : Forbidden 403
    @ExceptionHandler(ForbiddenResourceAccessException.class)
    @ResponseBody
    public ResponseEntity<CommonResponse<Void>> handleForbiddenResourceAccessException(ForbiddenResourceAccessException e) {
        log.error("[접근 권한 없음] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponse.error(403, e.getMessage()));
    }



}
