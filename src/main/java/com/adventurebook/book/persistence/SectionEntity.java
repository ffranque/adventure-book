package com.adventurebook.book.persistence;

import com.adventurebook.book.SectionType;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "section",
        uniqueConstraints = @UniqueConstraint(name = "uk_section_book_number",
                columnNames = {"book_id", "section_number"}))
public class SectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "section_seq")
    @SequenceGenerator(name = "section_seq", sequenceName = "section_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private int sectionNumber;

    @Column(length = 2000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SectionType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @OrderColumn(name = "option_order")
    private List<OptionEntity> options = new ArrayList<>();

    protected SectionEntity() {
    }

    SectionEntity(int sectionNumber, String text, SectionType type) {
        this.sectionNumber = sectionNumber;
        this.text = text;
        this.type = type;
    }

    void addOption(OptionEntity option) {
        options.add(option);
        option.setSection(this);
    }

    void setBook(BookEntity book) {
        this.book = book;
    }


    public Long getId() {
        return id;
    }

    public int getSectionNumber() {
        return sectionNumber;
    }

    public String getText() {
        return text;
    }

    public SectionType getType() {
        return type;
    }

    public BookEntity getBook() {
        return book;
    }

    public List<OptionEntity> getOptions() {
        return options;
    }
}
