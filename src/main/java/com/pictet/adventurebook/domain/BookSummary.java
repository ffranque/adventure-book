package com.pictet.adventurebook.domain;

import java.util.Set;

public record BookSummary(String id, String title, String author, Difficulty difficulty, Set<String> categories) {

    public BookSummary {
        categories = categories == null ? Set.of() : Set.copyOf(categories);
    }
}
