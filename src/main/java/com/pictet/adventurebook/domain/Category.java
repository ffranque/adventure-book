package com.pictet.adventurebook.domain;

public final class Category {

    private Category() {

    }

    public static String normalize(String category) {
        return category.trim().toUpperCase();
    }
}
