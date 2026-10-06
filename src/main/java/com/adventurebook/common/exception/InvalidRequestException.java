package com.adventurebook.common.exception;

public abstract class InvalidRequestException extends RuntimeException {

    protected InvalidRequestException(String message) {
        super(message);
    }

    protected InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
