package org.example.be.exception.custom;

// 자원 삭제가 실패한 경우
public class ResourceDeletionException extends RuntimeException {
    public ResourceDeletionException(String message) {
        super(message);
    }

    public ResourceDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
