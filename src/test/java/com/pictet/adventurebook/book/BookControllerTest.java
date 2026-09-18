package com.pictet.adventurebook.book;

import com.pictet.adventurebook.book.dto.AddCategoryRequest;
import com.pictet.adventurebook.book.dto.BookResponse;
import com.pictet.adventurebook.domain.Difficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookControllerTest {

    private final BookService bookService = mock(BookService.class);
    private final BookController bookController = new BookController(bookService);

    private BookResponse crystalCaverns;
    private BookResponse crystalCavernsWithCategory;

    @BeforeEach
    void setUp() {
        crystalCaverns = new BookResponse(
                "abc-123", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, Set.of());
        crystalCavernsWithCategory = new BookResponse(
                "abc-123", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, Set.of("HORROR"));
    }

    @Test
    void searchDelegatesFiltersToServiceAndReturnsResult() {
        when(bookService.search("crystal", "Evelyn", "horror", "EASY"))
                .thenReturn(List.of(crystalCaverns));

        List<BookResponse> result = bookController.search("crystal", "Evelyn", "horror", "EASY");

        assertThat(result).containsExactly(crystalCaverns);
    }

    @Test
    void searchWithNoFiltersPassesNullsThrough() {
        when(bookService.search(null, null, null, null)).thenReturn(List.of(crystalCaverns));

        List<BookResponse> result = bookController.search(null, null, null, null);

        assertThat(result).containsExactly(crystalCaverns);
    }

    @Test
    void getByIdReturnsBookFromService() {
        when(bookService.getById("abc-123")).thenReturn(crystalCavernsWithCategory);

        BookResponse result = bookController.getById("abc-123");

        assertThat(result).isEqualTo(crystalCavernsWithCategory);
    }

    @Test
    void addCategoryDelegatesIdAndCategoryToService() {
        when(bookService.addCategory("abc-123", "horror")).thenReturn(crystalCavernsWithCategory);

        BookResponse result = bookController.addCategory("abc-123", new AddCategoryRequest("horror"));

        assertThat(result).isEqualTo(crystalCavernsWithCategory);
        verify(bookService).addCategory("abc-123", "horror");
    }

    @Test
    void removeCategoryDelegatesIdAndCategoryToService() {
        when(bookService.removeCategory("abc-123", "HORROR")).thenReturn(crystalCaverns);

        BookResponse result = bookController.removeCategory("abc-123", "HORROR");

        assertThat(result).isEqualTo(crystalCaverns);
        verify(bookService).removeCategory("abc-123", "HORROR");
    }
}
