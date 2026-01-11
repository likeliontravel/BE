package org.example.be.exception.custom;

// 특정 자원의 수정에 실패한 경우 (또는 수정 후 저장이 실패한 경우)
public class ResourceUpdateException extends RuntimeException {
    public ResourceUpdateException(String message) {
        super(message);
    }

    public ResourceUpdateException(String message, Throwable cause) {
      super(message, cause);
    }
}
