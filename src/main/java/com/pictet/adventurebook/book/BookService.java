package com.pictet.adventurebook.book;

import com.pictet.adventurebook.book.dto.BookResponse;
import com.pictet.adventurebook.common.exception.InvalidDifficultyException;
import com.pictet.adventurebook.common.exception.book.BookNotFoundException;
import com.pictet.adventurebook.domain.Category;
import com.pictet.adventurebook.domain.Difficulty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookResponseMapper bookResponseMapper;

    BookService(BookRepository bookRepository, BookResponseMapper bookResponseMapper) {
        this.bookRepository = bookRepository;
        this.bookResponseMapper = bookResponseMapper;
    }

    public List<BookResponse> search(String title, String author, String category, String difficulty) {
        Difficulty parsedDifficulty = parseDifficultyOrThrow(difficulty);
        String normalizedCategory = category == null ? null : Category.normalize(category);

        return bookRepository.search(title, author, normalizedCategory, parsedDifficulty).stream()
                .map(bookResponseMapper::toBookResponse)
                .toList();
    }

    public BookResponse getById(String id) {
        return bookRepository.findSummaryById(id)
                .map(bookResponseMapper::toBookResponse)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public BookResponse addCategory(String id, String category) {
        return bookRepository.addCategory(id, Category.normalize(category))
                .map(bookResponseMapper::toBookResponse)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public BookResponse removeCategory(String id, String category) {
        return bookRepository.removeCategory(id, Category.normalize(category))
                .map(bookResponseMapper::toBookResponse)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private Difficulty parseDifficultyOrThrow(String difficulty) {
        if (difficulty == null) return null;

        try {
            return Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDifficultyException(difficulty);
        }
    }
}
