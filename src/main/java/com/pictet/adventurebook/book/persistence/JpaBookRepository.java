package com.pictet.adventurebook.book.persistence;

import com.pictet.adventurebook.book.BookRepository;
import com.pictet.adventurebook.domain.Book;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaBookRepository implements BookRepository {

    private final BookEntityRepository bookEntityRepository;
    private final BookEntityMapper bookEntityMapper;

    JpaBookRepository(BookEntityRepository bookEntityRepository, BookEntityMapper bookEntityMapper) {
        this.bookEntityRepository = bookEntityRepository;
        this.bookEntityMapper = bookEntityMapper;
    }

    @Override
    public List<Book> findAll() {
        return bookEntityRepository.findAll().stream()
                .map(bookEntityMapper::toBookDomain)
                .toList();
    }

    @Override
    public Optional<Book> findById(String id) {
        return bookEntityRepository.findById(id).map(bookEntityMapper::toBookDomain);
    }

    @Override
    @Transactional
    public Book save(Book book) {
        BookEntity saved = bookEntityRepository.save(bookEntityMapper.toBookEntity(book));
        return bookEntityMapper.toBookDomain(saved);
    }
}
