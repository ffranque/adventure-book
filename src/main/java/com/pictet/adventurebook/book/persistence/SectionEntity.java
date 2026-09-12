package com.pictet.adventurebook.book.persistence;

import com.pictet.adventurebook.domain.SectionType;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "section")
public class SectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int sectionNumber;

    @Column(length = 2000)
    private String text;

    @Enumerated(EnumType.STRING)
    private SectionType type;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private BookEntity book;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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
