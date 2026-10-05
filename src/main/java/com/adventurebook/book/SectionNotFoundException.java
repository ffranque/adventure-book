package com.adventurebook.book;

import com.adventurebook.common.exception.NotFoundException;

public class SectionNotFoundException extends NotFoundException {

    public SectionNotFoundException(String bookId, int sectionId) {
        super("No section " + sectionId + " found in book " + bookId);
    }
}
