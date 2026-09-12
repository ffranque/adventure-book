package com.pictet.adventurebook.book.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "option")
public class OptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private int gotoId;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private SectionEntity section;

    @Embedded
    private ConsequenceEmbeddable consequence;

    protected OptionEntity() {
    }

    OptionEntity(String description, int gotoId, ConsequenceEmbeddable consequence) {
        this.description = description;
        this.gotoId = gotoId;
        this.consequence = consequence;
    }

    void setSection(SectionEntity section) {
        this.section = section;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getGotoId() {
        return gotoId;
    }

    public SectionEntity getSection() {
        return section;
    }

    public ConsequenceEmbeddable getConsequence() {
        return consequence;
    }
}
