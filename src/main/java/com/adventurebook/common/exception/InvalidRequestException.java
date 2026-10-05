package com.adventurebook.common.exception;

/**
 * Thrown when a request is well-formed but its values are not acceptable. Mapped to 400.
 */
public abstract class InvalidRequestException extends RuntimeException {

    protected InvalidRequestException(String message) {
        super(message);
    }

    protected InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
