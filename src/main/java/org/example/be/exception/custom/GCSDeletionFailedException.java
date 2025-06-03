package org.example.be.exception.custom;

public class GCSDeletionFailedException extends RuntimeException {
    public GCSDeletionFailedException(String message) {
        super(message);
    }

    public GCSDeletionFailedException(String message, Throwable cause) {
      super(message, cause);
    }
}
