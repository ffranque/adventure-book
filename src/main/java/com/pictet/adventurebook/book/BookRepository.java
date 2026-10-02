package com.pictet.adventurebook.book;

import com.pictet.adventurebook.domain.Book;
import com.pictet.adventurebook.domain.BookSummary;
import com.pictet.adventurebook.domain.Difficulty;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    List<BookSummary> search(String title, String author, String category, Difficulty difficulty);

    Optional<BookSummary> findSummaryById(String id);

    Optional<Book> findById(String id);

    Book save(Book book);

    Optional<BookSummary> addCategory(String id, String category);

    Optional<BookSummary> removeCategory(String id, String category);
}
