package com.pictet.adventurebook.book;

import com.pictet.adventurebook.book.dto.BookResponse;
import com.pictet.adventurebook.common.exception.InvalidDifficultyException;
import com.pictet.adventurebook.common.exception.book.BookNotFoundException;
import com.pictet.adventurebook.domain.Book;
import com.pictet.adventurebook.domain.Difficulty;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BookServiceTest {

    private final BookRepository bookRepository = mock(BookRepository.class);
    private final BookService bookService = new BookService(bookRepository);

    private Book newBook(String id, String title, String author, Difficulty difficulty, String... categories) {
        return new Book(id, title, author, difficulty, new HashSet<>(Set.of(categories)), new HashMap<>());
    }

    @Test
    void searchWithNoFiltersReturnsAllBooks() {
        Book crystalCaverns = newBook("book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY);
        Book thePrisoner = newBook("book-2", "The Prisoner", "Daniel El Fuego", Difficulty.HARD);
        when(bookRepository.findAll()).thenReturn(List.of(crystalCaverns, thePrisoner));

        List<BookResponse> result = bookService.search(null, null, null, null);

        assertThat(result).extracting(BookResponse::id).containsExactly("book-1", "book-2");
    }

    @Test
    void searchFiltersByTitleCaseInsensitiveSubstring() {
        Book crystalCaverns = newBook("book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY);
        Book thePrisoner = newBook("book-2", "The Prisoner", "Daniel El Fuego", Difficulty.HARD);
        when(bookRepository.findAll()).thenReturn(List.of(crystalCaverns, thePrisoner));

        List<BookResponse> result = bookService.search("CRYSTAL", null, null, null);

        assertThat(result).extracting(BookResponse::id).containsExactly("book-1");
    }

    @Test
    void searchFiltersByAuthorCaseInsensitiveSubstring() {
        Book crystalCaverns = newBook("book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY);
        Book thePrisoner = newBook("book-2", "The Prisoner", "Daniel El Fuego", Difficulty.HARD);
        when(bookRepository.findAll()).thenReturn(List.of(crystalCaverns, thePrisoner));

        List<BookResponse> result = bookService.search(null, "fuego", null, null);

        assertThat(result).extracting(BookResponse::id).containsExactly("book-2");
    }

    @Test
    void searchFiltersByCategoryCaseInsensitive() {
        Book horrorBook = newBook("book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, "HORROR");
        Book plainBook = newBook("book-2", "The Prisoner", "Daniel El Fuego", Difficulty.HARD);
        when(bookRepository.findAll()).thenReturn(List.of(horrorBook, plainBook));

        List<BookResponse> result = bookService.search(null, null, "horror", null);

        assertThat(result).extracting(BookResponse::id).containsExactly("book-1");
    }

    @Test
    void searchFiltersByDifficulty() {
        Book easyBook = newBook("book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY);
        Book hardBook = newBook("book-2", "The Prisoner", "Daniel El Fuego", Difficulty.HARD);
        when(bookRepository.findAll()).thenReturn(List.of(easyBook, hardBook));

        List<BookResponse> result = bookService.search(null, null, null, "hard");

        assertThat(result).extracting(BookResponse::id).containsExactly("book-2");
    }

    @Test
    void searchWithInvalidDifficultyThrowsInvalidDifficultyException() {
        assertThatThrownBy(() -> bookService.search(null, null, null, "EXTREME"))
                .isInstanceOf(InvalidDifficultyException.class)
                .hasMessageContaining("EXTREME");
    }

    @Test
    void getByIdReturnsMatchingBook() {
        Book book = newBook("book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, "HORROR");
        when(bookRepository.findById("book-1")).thenReturn(Optional.of(book));

        BookResponse result = bookService.getById("book-1");

        assertThat(result).isEqualTo(new BookResponse(
                "book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, Set.of("HORROR")));
    }

    @Test
    void getByIdWithUnknownIdThrowsBookNotFoundException() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getById("missing"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void addCategoryNormalizesAndSavesCategory() {
        Book book = newBook("book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY);
        when(bookRepository.findById("book-1")).thenReturn(Optional.of(book));

        BookResponse result = bookService.addCategory("book-1", "  horror  ");

        assertThat(result.categories()).containsExactly("HORROR");
        verify(bookRepository).save(book);
    }

    @Test
    void removeCategoryNormalizesAndRemovesCategory() {
        Book book = newBook("book-1", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, "HORROR");
        when(bookRepository.findById("book-1")).thenReturn(Optional.of(book));

        BookResponse result = bookService.removeCategory("book-1", "  horror  ");

        assertThat(result.categories()).isEmpty();
        verify(bookRepository).save(book);
    }

    @Test
    void removeCategoryWithUnknownIdThrowsBookNotFoundException() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.removeCategory("missing", "horror"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
