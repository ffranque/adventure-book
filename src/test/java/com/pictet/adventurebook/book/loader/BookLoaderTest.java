package com.pictet.adventurebook.book.loader;

import com.pictet.adventurebook.common.exception.book.BookParsingException;
import com.pictet.adventurebook.domain.Book;
import com.pictet.adventurebook.domain.ConsequenceType;
import com.pictet.adventurebook.domain.Difficulty;
import com.pictet.adventurebook.domain.Section;
import com.pictet.adventurebook.domain.SectionType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookLoaderTest {

    private final BookLoader bookLoader = new BookLoader(JsonMapper.builder().build());

    private RawBook rawBookWith(String title, String author, String difficulty,
                                List<String> categories, List<RawSection> sections) {
        return new RawBook(title, author, difficulty, categories, sections);
    }

    private RawSection rawSection(int id, String type, RawOption... options) {
        return new RawSection(id, "some text", type, options.length == 0 ? null : List.of(options));
    }

    @Test
    void fromRawMapsAllFieldsToBook() {
        RawSection begin = rawSection(1, "BEGIN", new RawOption("go to end", 2, null));
        RawSection end = rawSection(2, "END");
        RawBook rawBook = rawBookWith("The Crystal Caverns", "Evelyn Stormrider", "easy",
                List.of("horror"), List.of(begin, end));

        Book book = bookLoader.fromRaw(rawBook);

        assertThat(book.getTitle()).isEqualTo("The Crystal Caverns");
        assertThat(book.getAuthor()).isEqualTo("Evelyn Stormrider");
        assertThat(book.getDifficulty()).isEqualTo(Difficulty.EASY);
        assertThat(book.getSections()).hasSize(2);
        assertThat(book.getSections().get(1).type()).isEqualTo(SectionType.BEGIN);
        assertThat(book.getSections().get(2).type()).isEqualTo(SectionType.END);
    }

    @Test
    void fromRawGeneratesEightCharacterAlphanumericId() {
        RawBook rawBook = rawBookWith("Title", "Author", "easy", null, List.of(rawSection(1, "END")));

        Book book = bookLoader.fromRaw(rawBook);

        assertThat(book.getId()).matches("[23456789abcdefghjkmnpqrstuvwxyz]{8}");
    }

    @Test
    void fromRawNormalizesCategoriesToUppercaseAndTrimmed() {
        RawBook rawBook = rawBookWith("Title", "Author", "easy",
                List.of("  horror ", "Puzzle"), List.of(rawSection(1, "END")));

        Book book = bookLoader.fromRaw(rawBook);

        assertThat(book.getCategories()).containsExactlyInAnyOrder("HORROR", "PUZZLE");
    }

    @Test
    void fromRawWithNullCategoriesReturnsEmptySet() {
        RawBook rawBook = rawBookWith("Title", "Author", "easy", null, List.of(rawSection(1, "END")));

        Book book = bookLoader.fromRaw(rawBook);

        assertThat(book.getCategories()).isEmpty();
    }

    @Test
    void fromRawWithBlankTitleThrowsBookParsingException() {
        RawBook rawBook = rawBookWith("  ", "Author", "easy", null, List.of(rawSection(1, "END")));

        assertThatThrownBy(() -> bookLoader.fromRaw(rawBook))
                .isInstanceOf(BookParsingException.class)
                .hasMessageContaining("title");
    }

    @Test
    void fromRawWithBlankAuthorThrowsBookParsingException() {
        RawBook rawBook = rawBookWith("Title", null, "easy", null, List.of(rawSection(1, "END")));

        assertThatThrownBy(() -> bookLoader.fromRaw(rawBook))
                .isInstanceOf(BookParsingException.class)
                .hasMessageContaining("author");
    }

    @Test
    void fromRawWithUnknownDifficultyThrowsBookParsingException() {
        RawBook rawBook = rawBookWith("Title", "Author", "EXTREME", null, List.of(rawSection(1, "END")));

        assertThatThrownBy(() -> bookLoader.fromRaw(rawBook))
                .isInstanceOf(BookParsingException.class)
                .hasMessageContaining("Difficulty")
                .hasMessageContaining("EXTREME");
    }

    @Test
    void fromRawWithUnknownSectionTypeThrowsBookParsingException() {
        RawBook rawBook = rawBookWith("Title", "Author", "easy", null, List.of(rawSection(1, "MIDDLE")));

        assertThatThrownBy(() -> bookLoader.fromRaw(rawBook))
                .isInstanceOf(BookParsingException.class)
                .hasMessageContaining("SectionType")
                .hasMessageContaining("MIDDLE");
    }

    @Test
    void fromRawWithDuplicateSectionIdsThrowsBookParsingException() {
        RawBook rawBook = rawBookWith("Title", "Author", "easy", null,
                List.of(rawSection(1, "BEGIN"), rawSection(1, "END")));

        assertThatThrownBy(() -> bookLoader.fromRaw(rawBook))
                .isInstanceOf(BookParsingException.class)
                .hasMessageContaining("duplicate section id: 1");
    }

    @Test
    void fromRawWithNullOptionsReturnsEmptyOptionList() {
        RawSection section = new RawSection(1, "text", "END", null);
        RawBook rawBook = rawBookWith("Title", "Author", "easy", null, List.of(section));

        Book book = bookLoader.fromRaw(rawBook);

        assertThat(book.getSections().get(1).options()).isEmpty();
    }

    @Test
    void fromRawWithNullConsequenceReturnsOptionWithNullConsequence() {
        RawSection section = rawSection(1, "END", new RawOption("no consequence", 1, null));
        RawBook rawBook = rawBookWith("Title", "Author", "easy", null, List.of(section));

        Book book = bookLoader.fromRaw(rawBook);

        assertThat(book.getSections().get(1).options().get(0).consequence()).isNull();
    }

    @Test
    void fromRawParsesConsequenceValueTrimmedToInt() {
        RawConsequence rawConsequence = new RawConsequence("LOSE_HEALTH", " 3 ", "ouch");
        RawSection section = rawSection(1, "END", new RawOption("hurts", 1, rawConsequence));
        RawBook rawBook = rawBookWith("Title", "Author", "easy", null, List.of(section));

        Book book = bookLoader.fromRaw(rawBook);

        var consequence = book.getSections().get(1).options().get(0).consequence();
        assertThat(consequence.type()).isEqualTo(ConsequenceType.LOSE_HEALTH);
        assertThat(consequence.value()).isEqualTo(3);
        assertThat(consequence.text()).isEqualTo("ouch");
    }

    @Test
    void fromRawWithNonNumericConsequenceValueThrowsBookParsingException() {
        RawConsequence rawConsequence = new RawConsequence("LOSE_HEALTH", "a lot", "ouch");
        RawSection section = rawSection(1, "END", new RawOption("hurts", 1, rawConsequence));
        RawBook rawBook = rawBookWith("Title", "Author", "easy", null, List.of(section));

        assertThatThrownBy(() -> bookLoader.fromRaw(rawBook))
                .isInstanceOf(BookParsingException.class)
                .hasMessageContaining("consequence value not numeric");
    }

    @Test
    void fromRawWithUnknownConsequenceTypeThrowsBookParsingException() {
        RawConsequence rawConsequence = new RawConsequence("EXPLODE", "3", "boom");
        RawSection section = rawSection(1, "END", new RawOption("hurts", 1, rawConsequence));
        RawBook rawBook = rawBookWith("Title", "Author", "easy", null, List.of(section));

        assertThatThrownBy(() -> bookLoader.fromRaw(rawBook))
                .isInstanceOf(BookParsingException.class)
                .hasMessageContaining("ConsequenceType")
                .hasMessageContaining("EXPLODE");
    }

    @Test
    void loadParsesValidJsonIntoBook() {
        String json = """
                {
                  "title": "The Crystal Caverns",
                  "author": "Evelyn Stormrider",
                  "difficulty": "easy",
                  "categories": ["horror"],
                  "sections": [
                    {"id": 1, "text": "start", "type": "BEGIN", "options": [
                      {"description": "go to end", "gotoId": 2}
                    ]},
                    {"id": 2, "text": "the end", "type": "END"}
                  ]
                }
                """;

        Book book = bookLoader.load(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

        assertThat(book.getTitle()).isEqualTo("The Crystal Caverns");
        Section beginSection = book.getSections().get(1);
        assertThat(beginSection.options()).hasSize(1);
        assertThat(beginSection.options().get(0).gotoId()).isEqualTo(2);
    }

    @Test
    void loadWithMalformedJsonThrowsBookParsingException() {
        String malformedJson = "{ not valid json";

        assertThatThrownBy(() -> bookLoader.load(
                new ByteArrayInputStream(malformedJson.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BookParsingException.class)
                .hasMessageContaining("Malformed JSON");
    }
}
