package com.pictet.adventurebook.book.persistence;

import com.pictet.adventurebook.domain.Difficulty;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "book")

public class BookEntity {

    @Id
    private String id;
    private String title;
    private String author;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "category")
    private Set<String> categories = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<SectionEntity> sections = new ArrayList<>();

    protected BookEntity() {
    }

    BookEntity(String id, String title, String author, Difficulty difficulty, Set<String> categories) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.difficulty = difficulty;
        this.categories = categories;
    }

    void addSection(SectionEntity section) {
        sections.add(section);
        section.setBook(this);
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

    public List<SectionEntity> getSections() {
        return sections;
    }
}
