package org.example.be.exception.custom;

public class GCSUploadFailedException extends RuntimeException {
    public GCSUploadFailedException(String message) {
        super(message);
    }

    public GCSUploadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
