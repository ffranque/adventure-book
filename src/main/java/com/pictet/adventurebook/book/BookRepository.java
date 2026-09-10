package com.pictet.adventurebook.book;

import com.pictet.adventurebook.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    List<Book> findAll();
    Optional<Book> findById(String id);
    Book save(Book book);
}
