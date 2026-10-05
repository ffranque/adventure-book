package com.adventurebook.book;

public final class Category {

    private Category() {

    }

    public static String normalize(String category) {
        return category.trim().toUpperCase();
    }
}
