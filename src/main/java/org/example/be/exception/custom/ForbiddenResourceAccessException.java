package org.example.be.exception.custom;

// 인증은 성공했으나 자원에 접근할 권한이 없어 수행이 불가능한 경우 던지는 예외
public class ForbiddenResourceAccessException extends RuntimeException {
    public ForbiddenResourceAccessException(String message) {
        super(message);
    }

    public ForbiddenResourceAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
