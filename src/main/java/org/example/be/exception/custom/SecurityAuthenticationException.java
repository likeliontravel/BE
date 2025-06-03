package org.example.be.exception.custom;

public class SecurityAuthenticationException extends RuntimeException {
    public SecurityAuthenticationException(String message) {
        super(message);
    }

    public SecurityAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
