package com.pictet.adventurebook.book.persistence;

import com.pictet.adventurebook.domain.Book;
import com.pictet.adventurebook.domain.Consequence;
import com.pictet.adventurebook.domain.Option;
import com.pictet.adventurebook.domain.Section;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class BookEntityMapper {

    BookEntity toBookEntity(Book book) {
        BookEntity entity = new BookEntity(book.getId(), book.getTitle(), book.getAuthor(),
                book.getDifficulty(), new HashSet<>(book.getCategories()));
        book.getSections().values().forEach(section -> entity.addSection(toSectionEntity(section)));

        return entity;
    }

    private SectionEntity toSectionEntity(Section section) {
        SectionEntity entity = new SectionEntity(section.id(), section.text(), section.type());
        section.options().forEach(option -> entity.addOption(toOptionEntity(option)));

        return entity;
    }

    private OptionEntity toOptionEntity(Option option) {
        ConsequenceEmbeddable consequence = option.consequence() == null
                ? null : toConsequenceEmbeddable(option.consequence());

        return new OptionEntity(option.description(), option.gotoId(), consequence);
    }

    private ConsequenceEmbeddable toConsequenceEmbeddable(Consequence consequence) {
        return new ConsequenceEmbeddable(consequence.type(), consequence.value(), consequence.text());
    }

    Book toBookDomain(BookEntity entity) {
        Map<Integer, Section> sections = entity.getSections().stream()
                .map(this::toSectionDomain)
                .collect(Collectors.toMap(Section::id, Function.identity()));

        return new Book(entity.getId(), entity.getTitle(), entity.getAuthor(), entity.getDifficulty(),
                new HashSet<>(entity.getCategories()), sections);
    }

    private Section toSectionDomain(SectionEntity entity) {
        List<Option> options = entity.getOptions().stream()
                .map(this::toOptionDomain)
                .toList();

        return new Section(entity.getSectionNumber(), entity.getText(), entity.getType(), options);
    }

    private Option toOptionDomain(OptionEntity entity) {
        Consequence consequence = entity.getConsequence() == null
                ? null : toConsequenceDomain(entity.getConsequence());

        return new Option(entity.getDescription(), entity.getGotoId(), consequence);
    }

    private Consequence toConsequenceDomain(ConsequenceEmbeddable entity) {
        return new Consequence(entity.getType(), entity.getValue(), entity.getText());
    }
}