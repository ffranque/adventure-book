package com.adventurebook.book.persistence;

import com.adventurebook.book.BookRepository;
import com.adventurebook.domain.Book;
import com.adventurebook.domain.BookSummary;
import com.adventurebook.domain.Difficulty;
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
    public List<BookSummary> search(String title, String author, String category, Difficulty difficulty) {
        return bookEntityRepository.search(title, author, category, difficulty)
                .stream()
                .map(bookEntityMapper::toBookSummary)
                .toList();
    }

    @Override
    public Optional<BookSummary> findSummaryById(String id) {
        return bookEntityRepository.findWithCategoriesById(id).map(bookEntityMapper::toBookSummary);
    }

    @Override
    public boolean existsById(String id) {
        return bookEntityRepository.existsById(id);
    }

    @Override
    public boolean existsBySource(String source) {
        return bookEntityRepository.existsBySource(source);
    }

    @Override
    public Book save(String source, Book book) {
        BookEntity saved = bookEntityRepository.save(bookEntityMapper.toBookEntity(source, book));
        return bookEntityMapper.toBookDomain(saved);
    }

    @Override
    @Transactional
    public Optional<BookSummary> addCategory(String id, String category) {
        return bookEntityRepository.findWithCategoriesById(id).map(entity -> {
            entity.getCategories().add(category);
            return bookEntityMapper.toBookSummary(entity);
        });
    }

    @Override
    @Transactional
    public Optional<BookSummary> removeCategory(String id, String category) {
        return bookEntityRepository.findWithCategoriesById(id).map(entity -> {
            entity.getCategories().remove(category);
            return bookEntityMapper.toBookSummary(entity);
        });
    }
}
