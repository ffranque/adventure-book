package com.pictet.adventurebook.common.exception.adventure;

public class SectionNotFoundException extends RuntimeException {

    public SectionNotFoundException(String bookId, int sectionId) {
        super("No section " + sectionId + " found in book " + bookId);
    }
}
