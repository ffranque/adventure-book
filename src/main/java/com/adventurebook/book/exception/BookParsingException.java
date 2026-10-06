package com.adventurebook.book.exception;

public class BookParsingException extends RuntimeException {

    public BookParsingException(String message) {
        super(message);
    }

    public BookParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
