package org.example.be.exception.custom;

public class InvalidInvitationException extends RuntimeException {
    public InvalidInvitationException(String message) {
        super(message);

    }

    public InvalidInvitationException(String message, Throwable cause) {
      super(message, cause);
    }
}
