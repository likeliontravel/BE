package org.example.be.exception.custom;

public class BadParameterException extends RuntimeException {
    public BadParameterException(String message) {
        super(message);
    }

    public BadParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
