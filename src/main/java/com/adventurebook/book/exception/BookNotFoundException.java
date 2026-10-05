package com.adventurebook.book.exception;

import com.adventurebook.common.exception.NotFoundException;

public class BookNotFoundException extends NotFoundException {

    public BookNotFoundException(String id) {
        super("No book found with id: " + id);
    }
}
