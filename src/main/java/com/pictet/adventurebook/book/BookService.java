package com.pictet.adventurebook.book;

import com.pictet.adventurebook.book.dto.BookResponse;
import com.pictet.adventurebook.common.exception.InvalidDifficultyException;
import com.pictet.adventurebook.common.exception.book.BookNotFoundException;
import com.pictet.adventurebook.domain.BookSummary;
import com.pictet.adventurebook.domain.Difficulty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookResponseMapper bookResponseMapper;

    public BookService(BookRepository bookRepository, BookResponseMapper bookResponseMapper) {
        this.bookRepository = bookRepository;
        this.bookResponseMapper = bookResponseMapper;
    }

    public List<BookResponse> search(String title, String author, String category, String difficulty) {
        Difficulty parsedDifficulty = parseDifficultyOrThrow(difficulty);
        String normalizedCategory = category == null ? null : normalizeCategory(category);

        return bookRepository.search(title, author, normalizedCategory, parsedDifficulty).stream()
                .map(bookResponseMapper::toBookResponse)
                .toList();
    }

    public BookResponse getById(String id) {
        return toResponseOrThrow(id, bookRepository.findSummaryById(id));
    }

    public BookResponse addCategory(String id, String category) {
        return toResponseOrThrow(id, bookRepository.addCategory(id, normalizeCategory(category)));
    }

    public BookResponse removeCategory(String id, String category) {
        return toResponseOrThrow(id, bookRepository.removeCategory(id, normalizeCategory(category)));
    }

    private Difficulty parseDifficultyOrThrow(String difficulty) {
        if (difficulty == null) return null;

        try {
            return Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDifficultyException(difficulty);
        }
    }

    private BookResponse toResponseOrThrow(String id, Optional<BookSummary> book) {
        return book.map(bookResponseMapper::toBookResponse)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private String normalizeCategory(String category) {
        return category.trim().toUpperCase();
    }
}
