package com.adventurebook.common.exception;

/**
 * Thrown when a request conflicts with the current state of a resource. Mapped to 409.
 */
public abstract class ConflictException extends RuntimeException {

    protected ConflictException(String message) {
        super(message);
    }

    protected ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
