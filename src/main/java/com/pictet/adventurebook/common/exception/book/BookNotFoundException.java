package com.pictet.adventurebook.common.exception.book;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String id) {
        super("No book found with id: " + id);
    }
}
