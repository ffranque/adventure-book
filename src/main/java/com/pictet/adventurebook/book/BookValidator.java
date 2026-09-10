package com.pictet.adventurebook.book;

import com.pictet.adventurebook.domain.Book;
import com.pictet.adventurebook.domain.Option;
import com.pictet.adventurebook.domain.Section;
import com.pictet.adventurebook.domain.SectionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookValidator {

    public List<String> validate(Book book) {
        List<String> violations = new ArrayList<>();

        checkSingleBeginning(book, violations);
        checkHasEnding(book, violations);
        checkGotoIdsResolve(book, violations);
        checkNonEndingSectionsHaveOptions(book, violations);

        return violations;
    }

    private void checkSingleBeginning(Book book, List<String> violations) {
        long beginCount = book.getSections().values().stream()
                .filter(s -> s.type() == SectionType.BEGIN)
                .count();

        if (beginCount != 1) {
            violations.add("Book must have exactly one BEGIN section, found " + beginCount);
        }
    }

    private void checkHasEnding(Book book, List<String> violations) {
        boolean hasEnding = book.getSections().values().stream()
                .anyMatch(s -> s.type() == SectionType.END);

        if (!hasEnding) {
            violations.add("Book must have at least one END section");
        }
    }

    private void checkGotoIdsResolve(Book book, List<String> violations) {
        for (Section section : book.getSections().values()) {
            for (Option option : section.options()) {
                if (!book.getSections().containsKey(option.gotoId())) {
                    violations.add("Section " + section.id() + " option \"" + option.description()
                            + "\" points to non-existent section id " + option.gotoId());
                }
            }
        }
    }

    private void checkNonEndingSectionsHaveOptions(Book book, List<String> violations) {
        for (Section section : book.getSections().values()) {
            if (section.type() != SectionType.END && section.options().isEmpty()) {
                violations.add("Section " + section.id() + " is type " + section.type() + " but has no options");
            }
        }
    }
}
