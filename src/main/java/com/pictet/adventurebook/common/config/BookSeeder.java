package com.pictet.adventurebook.common.config;

import com.pictet.adventurebook.book.BookRepository;
import com.pictet.adventurebook.book.BookValidator;
import com.pictet.adventurebook.book.loader.BookLoader;
import com.pictet.adventurebook.common.exception.book.BookParsingException;
import com.pictet.adventurebook.domain.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Component
public class BookSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BookSeeder.class);
    private static final String BOOKS_LOCATION_PATTERN = "classpath:books/*.json";

    private final ResourcePatternResolver resourcePatternResolver;
    private final BookLoader bookLoader;
    private final BookValidator bookValidator;
    private final BookRepository bookRepository;

    public BookSeeder(ResourcePatternResolver resourcePatternResolver, BookLoader bookLoader,
                      BookValidator bookValidator, BookRepository bookRepository) {

        this.resourcePatternResolver = resourcePatternResolver;
        this.bookLoader = bookLoader;
        this.bookValidator = bookValidator;
        this.bookRepository = bookRepository;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        Resource [] resources = resourcePatternResolver.getResources(BOOKS_LOCATION_PATTERN);

        if (resources.length == 0) {
            log.warn("No books found in " + BOOKS_LOCATION_PATTERN);
        }

        int loadedBooks = 0;
        for (Resource resource : resources) {
            if(tryLoadBook(resource).isPresent()){
                loadedBooks++;
            }
        }

        log.info("Book loading complete for {} books", loadedBooks);
    }

    private Optional<Book> tryLoadBook(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            Book book = bookLoader.load(inputStream);
            List<String> violations = bookValidator.validate(book);
            if (!violations.isEmpty()) {
                log.warn("Skipping invalid book {}: {}", resource.getFilename(), violations);
                return Optional.empty();
            }

            bookRepository.save(book);
            log.info("Loaded book: {} ({})", book.getTitle(), resource.getFilename());
            return Optional.of(book);
        } catch (IOException e) {
            log.warn("Skipping unreadable resource {}: {}", resource.getFilename(), e.getMessage());
            return Optional.empty();
        } catch (BookParsingException e) {
            log.warn("Skipping unparseable book {}: {}", resource.getFilename(), e.getMessage());
            return Optional.empty();
        }
    }
}
