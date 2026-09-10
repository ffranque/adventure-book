package com.pictet.adventurebook.domain;

import java.util.Map;
import java.util.Set;

public class Book {

    String id;
    String title;
    String author;
    Difficulty difficulty;
    Set<String> categories;
    Map<Integer, Section> sections;

    public Book(String id, String title, String author, Difficulty difficulty, Set<String> categories,  Map<Integer, Section> sections) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.difficulty = difficulty;
        this.categories = categories;
        this.sections = sections;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public Map<Integer, Section> getSections() {
        return sections;
    }
}
