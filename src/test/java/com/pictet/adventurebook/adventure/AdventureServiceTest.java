package com.pictet.adventurebook.adventure;

import com.pictet.adventurebook.adventure.dto.PlayResultResponse;
import com.pictet.adventurebook.adventure.dto.SectionResponse;
import com.pictet.adventurebook.book.BookRepository;
import com.pictet.adventurebook.common.exception.adventure.InvalidOptionException;
import com.pictet.adventurebook.common.exception.adventure.SectionNotFoundException;
import com.pictet.adventurebook.common.exception.book.BookNotFoundException;
import com.pictet.adventurebook.domain.Book;
import com.pictet.adventurebook.domain.Consequence;
import com.pictet.adventurebook.domain.ConsequenceType;
import com.pictet.adventurebook.domain.Difficulty;
import com.pictet.adventurebook.domain.Option;
import com.pictet.adventurebook.domain.Section;
import com.pictet.adventurebook.domain.SectionType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdventureServiceTest {

    private final BookRepository bookRepository = mock(BookRepository.class);
    private final AdventureService adventureService = new AdventureService(bookRepository);

    private Book bookWithSections(Section... sections) {
        Map<Integer, Section> sectionsById = new HashMap<>();
        for (Section section : sections) {
            sectionsById.put(section.id(), section);
        }
        return new Book("book-1", "Title", "Author", Difficulty.EASY, Set.of(), sectionsById);
    }

    private void givenBook(Book book) {
        when(bookRepository.findById("book-1")).thenReturn(Optional.of(book));
    }

    @Test
    void beginReturnsTheBeginSectionMappedToResponse() {
        Section begin = new Section(1, "You stand at the entrance.", SectionType.BEGIN,
                List.of(new Option("Go in", 2, null)));
        Section end = new Section(2, "The end.", SectionType.END, List.of());
        givenBook(bookWithSections(begin, end));

        SectionResponse result = adventureService.begin("book-1");

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.type()).isEqualTo(SectionType.BEGIN);
        assertThat(result.options()).hasSize(1);
        assertThat(result.options().get(0).index()).isEqualTo(0);
        assertThat(result.options().get(0).gotoId()).isEqualTo(2);
    }

    @Test
    void beginWithUnknownBookThrowsBookNotFoundException() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adventureService.begin("missing"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void beginWithNoBeginSectionThrowsIllegalStateException() {
        Section end = new Section(1, "The end.", SectionType.END, List.of());
        givenBook(bookWithSections(end));

        assertThatThrownBy(() -> adventureService.begin("book-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("book-1");
    }

    @Test
    void getSectionReturnsMatchingSectionMappedToResponse() {
        Section node = new Section(5, "A fork in the path.", SectionType.NODE,
                List.of(new Option("Left", 6, null), new Option("Right", 7, null)));
        givenBook(bookWithSections(node));

        SectionResponse result = adventureService.getSection("book-1", 5);

        assertThat(result.id()).isEqualTo(5);
        assertThat(result.options()).extracting(o -> o.index()).containsExactly(0, 1);
    }

    @Test
    void getSectionWithUnknownBookThrowsBookNotFoundException() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adventureService.getSection("missing", 1))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void getSectionWithUnknownSectionIdThrowsSectionNotFoundException() {
        Section begin = new Section(1, "start", SectionType.BEGIN, List.of());
        givenBook(bookWithSections(begin));

        assertThatThrownBy(() -> adventureService.getSection("book-1", 999))
                .isInstanceOf(SectionNotFoundException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("book-1");
    }

    @Test
    void chooseMovesToNextSectionWhenOptionHasNoConsequence() {
        Section start = new Section(1, "start", SectionType.BEGIN, List.of(new Option("Go", 2, null)));
        Section next = new Section(2, "middle", SectionType.NODE, List.of());
        givenBook(bookWithSections(start, next));

        PlayResultResponse result = adventureService.choose("book-1", 1, 0, 10);

        assertThat(result.section().id()).isEqualTo(2);
        assertThat(result.health()).isEqualTo(10);
        assertThat(result.consequenceText()).isNull();
        assertThat(result.dead()).isFalse();
        assertThat(result.gameOver()).isFalse();
    }

    @Test
    void chooseAppliesLoseHealthConsequence() {
        Consequence loseHealth = new Consequence(ConsequenceType.LOSE_HEALTH, 3, "You got hurt");
        Section start = new Section(1, "start", SectionType.BEGIN,
                List.of(new Option("Fight", 2, loseHealth)));
        Section next = new Section(2, "middle", SectionType.NODE, List.of());
        givenBook(bookWithSections(start, next));

        PlayResultResponse result = adventureService.choose("book-1", 1, 0, 10);

        assertThat(result.health()).isEqualTo(7);
        assertThat(result.consequenceText()).isEqualTo("You got hurt");
        assertThat(result.dead()).isFalse();
    }

    @Test
    void chooseAppliesGainHealthConsequenceClampedAtMaxHealth() {
        Consequence gainHealth = new Consequence(ConsequenceType.GAIN_HEALTH, 5, "You feel better");
        Section start = new Section(1, "start", SectionType.BEGIN,
                List.of(new Option("Rest", 2, gainHealth)));
        Section next = new Section(2, "middle", SectionType.NODE, List.of());
        givenBook(bookWithSections(start, next));

        PlayResultResponse result = adventureService.choose("book-1", 1, 0, 8);

        assertThat(result.health()).isEqualTo(10);
    }

    @Test
    void chooseClampsHealthLossAtMinHealthAndMarksDead() {
        Consequence loseHealth = new Consequence(ConsequenceType.LOSE_HEALTH, 20, "Fatal blow");
        Section start = new Section(1, "start", SectionType.BEGIN,
                List.of(new Option("Fight", 2, loseHealth)));
        Section next = new Section(2, "middle", SectionType.NODE, List.of());
        givenBook(bookWithSections(start, next));

        PlayResultResponse result = adventureService.choose("book-1", 1, 0, 10);

        assertThat(result.health()).isEqualTo(0);
        assertThat(result.dead()).isTrue();
        assertThat(result.gameOver()).isTrue();
    }

    @Test
    void chooseMarksGameOverWhenNextSectionIsEnd() {
        Section start = new Section(1, "start", SectionType.BEGIN, List.of(new Option("Finish", 2, null)));
        Section end = new Section(2, "the end", SectionType.END, List.of());
        givenBook(bookWithSections(start, end));

        PlayResultResponse result = adventureService.choose("book-1", 1, 0, 10);

        assertThat(result.gameOver()).isTrue();
        assertThat(result.dead()).isFalse();
    }

    @Test
    void chooseWithOptionIndexTooHighThrowsInvalidOptionException() {
        Section start = new Section(1, "start", SectionType.BEGIN, List.of(new Option("Only option", 2, null)));
        Section next = new Section(2, "middle", SectionType.NODE, List.of());
        givenBook(bookWithSections(start, next));

        assertThatThrownBy(() -> adventureService.choose("book-1", 1, 5, 10))
                .isInstanceOf(InvalidOptionException.class)
                .hasMessageContaining("5")
                .hasMessageContaining("1");
    }

    @Test
    void chooseWithUnknownBookThrowsBookNotFoundException() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adventureService.choose("missing", 1, 0, 10))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void chooseWithUnknownSectionIdThrowsSectionNotFoundException() {
        Section start = new Section(1, "start", SectionType.BEGIN, List.of());
        givenBook(bookWithSections(start));

        assertThatThrownBy(() -> adventureService.choose("book-1", 999, 0, 10))
                .isInstanceOf(SectionNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void chooseWithGotoIdPointingToUnknownSectionThrowsSectionNotFoundException() {
        Section start = new Section(1, "start", SectionType.BEGIN, List.of(new Option("Go nowhere", 404, null)));
        givenBook(bookWithSections(start));

        assertThatThrownBy(() -> adventureService.choose("book-1", 1, 0, 10))
                .isInstanceOf(SectionNotFoundException.class)
                .hasMessageContaining("404");
    }
}
