package com.adventurebook.book.domain;

import java.util.Map;
import java.util.Set;

public record Book(String id, String title, String author, Difficulty difficulty, Set<String> categories,
                   Map<Integer, Section> sections) {

    public Book {
        categories = categories == null ? Set.of() : Set.copyOf(categories);
        sections = sections == null ? Map.of() : Map.copyOf(sections);
    }
}
