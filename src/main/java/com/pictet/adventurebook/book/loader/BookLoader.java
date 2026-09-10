package com.pictet.adventurebook.book.loader;

import com.pictet.adventurebook.common.exception.book.BookParsingException;
import com.pictet.adventurebook.domain.*;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class BookLoader {

    private static final String ID_ALPHABET = "23456789abcdefghjkmnpqrstuvwxyz";
    private static final int ID_LENGTH = 8;

    private final JsonMapper jsonMapper;

    public BookLoader(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public Book load(InputStream jsonStream) {
        RawBook rawBook;
        try {
            rawBook = jsonMapper.readValue(jsonStream, RawBook.class);
        } catch (JacksonException e) {
            throw new BookParsingException("Malformed JSON", e);
        }
        return toBook(rawBook);
    }

    private Book toBook(RawBook rawBook) {
        Set<String> categories = rawBook.categories() == null
                ? new HashSet<>()
                : new HashSet<>(rawBook.categories());

        Map<Integer, Section> sections = rawBook.sections().stream()
                .collect(Collectors.toMap(RawSection::id, this::toSection, (a, b) -> {
                    throw new BookParsingException("duplicate section id: " + a.id());
                }));

        return new Book(generateId(), rawBook.title(), rawBook.author(),
                parseEnum(Difficulty.class, rawBook.difficulty()), categories, sections);
    }

    private Section toSection(RawSection rawSection) {
        List<Option> options = rawSection.options() == null
                ? List.of()
                : rawSection.options().stream()
                .map(this::toOption)
                .toList();

        return new Section(rawSection.id(), rawSection.text(), parseEnum(SectionType.class, rawSection.type()), options);
    }

    private Option toOption(RawOption rawOption) {
        Consequence consequence = rawOption.consequence() == null ? null : toConsequence(rawOption.consequence());

        return new Option(rawOption.description(), rawOption.gotoId(), consequence);
    }

    private Consequence toConsequence(RawConsequence rawConsequence) {
        int value;
        try {
            value = Integer.parseInt(rawConsequence.value().trim());
        } catch (NumberFormatException e) {
            throw new BookParsingException("consequence value not numeric: " + rawConsequence.value());
        }

        return new Consequence(parseEnum(ConsequenceType.class, rawConsequence.type()), value, rawConsequence.text());
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value) {
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BookParsingException("unknown Enum " + type.getSimpleName() + ": " + value);
        }
    }

    private String generateId() {
        StringBuilder id = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            id.append(ID_ALPHABET.charAt(ThreadLocalRandom.current().nextInt(ID_ALPHABET.length())));
        }
        return id.toString();
    }
}
