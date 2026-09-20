package com.pictet.adventurebook.book;

import com.pictet.adventurebook.book.dto.BookResponse;
import com.pictet.adventurebook.common.exception.InvalidDifficultyException;
import com.pictet.adventurebook.common.exception.book.BookNotFoundException;
import com.pictet.adventurebook.domain.Book;
import com.pictet.adventurebook.domain.Difficulty;
import org.springframework.stereotype.Service;

import java.util.List;

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

        return bookRepository.findAll().stream()
                .filter(b -> title == null || containsIgnoreCase(b.getTitle(), title))
                .filter(b -> author == null || containsIgnoreCase(b.getAuthor(), author))
                .filter(b -> category == null || matchesCategory(b, category))
                .filter(b -> parsedDifficulty == null || b.getDifficulty() == parsedDifficulty)
                .map(bookResponseMapper::toBookResponse)
                .toList();
    }

    public BookResponse getById(String id) {
        return bookResponseMapper.toBookResponse(findOrThrow(id));
    }

    public BookResponse addCategory(String id, String category) {
        Book book = findOrThrow(id);
        book.getCategories().add(normalizeCategory(category));
        bookRepository.save(book);

        return bookResponseMapper.toBookResponse(book);
    }

    public BookResponse removeCategory(String id, String category) {
        Book book = findOrThrow(id);
        book.getCategories().remove(normalizeCategory(category));
        bookRepository.save(book);

        return bookResponseMapper.toBookResponse(book);
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source.toLowerCase().contains(target.toLowerCase());
    }

    private boolean matchesCategory(Book book, String category) {
        return book.getCategories().stream().anyMatch(c -> c.equalsIgnoreCase(category));
    }

    private Difficulty parseDifficultyOrThrow(String difficulty) {
        if (difficulty == null) return null;

        try {
            return Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDifficultyException(difficulty);
        }
    }

    private Book findOrThrow(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private String normalizeCategory(String category) {
        return category.trim().toUpperCase();
    }
}
