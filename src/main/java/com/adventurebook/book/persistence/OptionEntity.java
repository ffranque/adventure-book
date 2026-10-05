package com.adventurebook.book.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "section_option",
        indexes = @Index(name = "idx_section_option_section", columnList = "section_id"))
public class OptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "section_option_seq")
    @SequenceGenerator(name = "section_option_seq", sequenceName = "section_option_seq", allocationSize = 50)
    private Long id;
    private String description;

    @Column(nullable = false)
    private int gotoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
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
