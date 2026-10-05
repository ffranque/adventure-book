package com.adventurebook.book;

import com.adventurebook.book.domain.Book;
import com.adventurebook.book.domain.BookSummary;
import com.adventurebook.book.domain.Difficulty;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    List<BookSummary> search(String title, String author, String category, Difficulty difficulty);

    Optional<BookSummary> findSummaryById(String id);

    boolean existsById(String id);

    boolean existsBySource(String source);

    Book save(String source, Book book);

    Optional<BookSummary> addCategory(String id, String category);

    Optional<BookSummary> removeCategory(String id, String category);
}
