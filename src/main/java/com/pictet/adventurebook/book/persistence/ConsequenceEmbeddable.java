package com.pictet.adventurebook.book.persistence;

import com.pictet.adventurebook.domain.ConsequenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ConsequenceEmbeddable {

    @Enumerated(EnumType.STRING)
    private ConsequenceType type;

    @Column(name = "consequence_value")
    private int value;

    @Column(length = 1000)
    private String text;

    protected ConsequenceEmbeddable() {
    }

    ConsequenceEmbeddable(ConsequenceType type, int value, String text) {
        this.type = type;
        this.value = value;
        this.text = text;
    }

    public ConsequenceType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
