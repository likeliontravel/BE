package org.example.be.exception.custom;

public class UserAuthenticationNotFoundException extends RuntimeException {
    public UserAuthenticationNotFoundException(String message) {
        super(message);
    }

    public UserAuthenticationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
