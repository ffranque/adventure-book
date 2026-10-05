package com.adventurebook.book.persistence;

import com.adventurebook.book.domain.Difficulty;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "book")
public class BookEntity {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String source;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @ElementCollection
    @CollectionTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"),
            indexes = @Index(name = "idx_book_categories_category", columnList = "category"))
    @Column(name = "category", nullable = false)
    private Set<String> categories = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<SectionEntity> sections = new ArrayList<>();

    protected BookEntity() {
    }

    BookEntity(String source, String title, String author, Difficulty difficulty, Set<String> categories) {
        this.source = source;
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

    public String getSource() {
        return source;
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
