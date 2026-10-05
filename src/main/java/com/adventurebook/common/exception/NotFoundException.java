package com.adventurebook.common.exception;

/**
 * Thrown when a requested resource doesn't exist. Mapped to 404.
 */
public abstract class NotFoundException extends RuntimeException {

    protected NotFoundException(String message) {
        super(message);
    }

    protected NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
