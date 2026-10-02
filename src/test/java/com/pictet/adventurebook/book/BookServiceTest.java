package com.pictet.adventurebook.book;

import com.pictet.adventurebook.book.dto.BookResponse;
import com.pictet.adventurebook.common.exception.InvalidDifficultyException;
import com.pictet.adventurebook.common.exception.book.BookNotFoundException;
import com.pictet.adventurebook.domain.BookSummary;
import com.pictet.adventurebook.domain.Difficulty;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BookServiceTest {

    private final BookRepository bookRepository = mock(BookRepository.class);
    private final BookResponseMapper bookResponseMapper = Mappers.getMapper(BookResponseMapper.class);
    private final BookService bookService = new BookService(bookRepository, bookResponseMapper);

    private final BookSummary crystalCaverns = new BookSummary(
            "book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, Set.of("HORROR"));
    private final BookSummary thePrisoner = new BookSummary(
            "book-2", "The Prisoner", "Daniel El Fuego", Difficulty.HARD, Set.of());

    @Test
    void searchWithNoFiltersDelegatesWithNullFilters() {
        when(bookRepository.search(null, null, null, null)).thenReturn(List.of(crystalCaverns, thePrisoner));

        List<BookResponse> result = bookService.search(null, null, null, null);

        assertThat(result).extracting(BookResponse::id).containsExactly("book-1", "book-2");
    }

    @Test
    void searchPassesTitleAndAuthorThroughUnchanged() {
        when(bookRepository.search("CRYSTAL", "storm", null, null)).thenReturn(List.of(crystalCaverns));

        List<BookResponse> result = bookService.search("CRYSTAL", "storm", null, null);

        assertThat(result).extracting(BookResponse::id).containsExactly("book-1");
    }

    @Test
    void searchNormalizesCategory() {
        when(bookRepository.search(null, null, "HORROR", null)).thenReturn(List.of(crystalCaverns));

        List<BookResponse> result = bookService.search(null, null, "  horror ", null);

        assertThat(result).extracting(BookResponse::id).containsExactly("book-1");
    }

    @Test
    void searchParsesDifficultyCaseInsensitive() {
        when(bookRepository.search(null, null, null, Difficulty.HARD)).thenReturn(List.of(thePrisoner));

        List<BookResponse> result = bookService.search(null, null, null, "hard");

        assertThat(result).extracting(BookResponse::id).containsExactly("book-2");
    }

    @Test
    void searchWithInvalidDifficultyThrowsInvalidDifficultyException() {
        assertThatThrownBy(() -> bookService.search(null, null, null, "EXTREME"))
                .isInstanceOf(InvalidDifficultyException.class)
                .hasMessageContaining("EXTREME");
        verifyNoInteractions(bookRepository);
    }

    @Test
    void getByIdReturnsMatchingBook() {
        when(bookRepository.findSummaryById("book-1")).thenReturn(Optional.of(crystalCaverns));

        BookResponse result = bookService.getById("book-1");

        assertThat(result).isEqualTo(new BookResponse(
                "book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, Set.of("HORROR")));
    }

    @Test
    void getByIdWithUnknownIdThrowsBookNotFoundException() {
        when(bookRepository.findSummaryById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getById("missing"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void addCategoryNormalizesBeforeDelegating() {
        when(bookRepository.addCategory("book-1", "HORROR")).thenReturn(Optional.of(crystalCaverns));

        BookResponse result = bookService.addCategory("book-1", "  horror  ");

        assertThat(result.categories()).containsExactly("HORROR");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void addCategoryWithUnknownIdThrowsBookNotFoundException() {
        when(bookRepository.addCategory("missing", "HORROR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.addCategory("missing", "horror"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void removeCategoryNormalizesBeforeDelegating() {
        when(bookRepository.removeCategory("book-2", "HORROR")).thenReturn(Optional.of(thePrisoner));

        BookResponse result = bookService.removeCategory("book-2", "  horror  ");

        assertThat(result.categories()).isEmpty();
        verify(bookRepository, never()).save(any());
    }

    @Test
    void removeCategoryWithUnknownIdThrowsBookNotFoundException() {
        when(bookRepository.removeCategory("missing", "HORROR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.removeCategory("missing", "horror"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
