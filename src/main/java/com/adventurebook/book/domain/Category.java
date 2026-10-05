package com.adventurebook.book.domain;

public final class Category {

    private Category() {

    }

    public static String normalize(String category) {
        return category.trim().toUpperCase();
    }
}
