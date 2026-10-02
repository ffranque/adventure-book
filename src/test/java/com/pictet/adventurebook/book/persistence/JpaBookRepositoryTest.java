package com.pictet.adventurebook.book.persistence;

import com.pictet.adventurebook.domain.*;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Not transactional: every repository call commits on its own, exactly as in production,
// so lazy-loading outside a transaction and real flush behaviour are exercised.
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({JpaBookRepository.class, BookEntityMapper.class})
class JpaBookRepositoryTest {

    @Autowired
    private JpaBookRepository repository;

    @Autowired
    private BookEntityRepository bookEntityRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        repository.save(book("cc", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, Set.of("HORROR")));
        repository.save(book("tp", "The Prisoner", "Daniel El Fuego", Difficulty.HARD, Set.of("ESCAPE")));
        repository.save(book("pj", "Pirates of the 100%_Jade Sea", "Anne Bonny", Difficulty.MEDIUM, Set.of()));

        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @AfterEach
    void tearDown() {
        bookEntityRepository.deleteAll();
    }

    @Test
    void searchWithoutFiltersReturnsAllBooksOrderedByTitle() {
        assertThat(repository.search(null, null, null, null))
                .extracting(BookSummary::id)
                .containsExactly("pj", "cc", "tp");
    }

    @Test
    void searchMatchesTitleAndAuthorAsCaseInsensitiveSubstrings() {
        assertThat(repository.search("CRYSTAL", null, null, null)).extracting(BookSummary::id).containsExactly("cc");
        assertThat(repository.search(null, "fuego", null, null)).extracting(BookSummary::id).containsExactly("tp");
    }

    @Test
    void searchTreatsLikeWildcardsInInputLiterally() {
        assertThat(repository.search("100%_", null, null, null)).extracting(BookSummary::id).containsExactly("pj");
        assertThat(repository.search("%", null, null, null)).extracting(BookSummary::id).containsExactly("pj");
        assertThat(repository.search("_", null, null, null)).extracting(BookSummary::id).containsExactly("pj");
    }

    @Test
    void searchByCategoryStillReturnsEveryCategoryOfTheMatchingBook() {
        repository.addCategory("cc", "CAVES");

        List<BookSummary> result = repository.search(null, null, "HORROR", null);

        assertThat(result).extracting(BookSummary::id).containsExactly("cc");
        assertThat(result.getFirst().categories()).containsExactlyInAnyOrder("HORROR", "CAVES");
    }

    @Test
    void searchByDifficulty() {
        assertThat(repository.search(null, null, null, Difficulty.HARD)).extracting(BookSummary::id).containsExactly("tp");
    }

    @Test
    void searchDoesNotLoadSectionsOrOptions() {
        repository.search(null, null, null, null);

        assertThat(statistics.getEntityLoadCount())
                .as("only the 3 BookEntity rows should be loaded")
                .isEqualTo(3);
    }

    @Test
    void findByIdLoadsSectionsAndOptionsOutsideCallerTransaction() {
        Book book = repository.findById("cc").orElseThrow();

        assertThat(book.getSections()).containsOnlyKeys(1, 2);
        assertThat(book.getSections().get(1).options())
                .extracting(Option::gotoId)
                .containsExactly(2, 2);
        assertThat(book.getSections().get(1).options().get(1).consequence())
                .isEqualTo(new Consequence(ConsequenceType.LOSE_HEALTH, 3, "Ouch"));
    }

    @Test
    void findSummaryByIdReturnsMetadataWithoutLoadingSections() {
        BookSummary summary = repository.findSummaryById("cc").orElseThrow();

        assertThat(summary.categories()).containsExactly("HORROR");
        assertThat(statistics.getEntityLoadCount()).isEqualTo(1);
    }

    @Test
    void addCategoryOnlyTouchesTheCategoryTable() {
        BookSummary result = repository.addCategory("cc", "CAVES").orElseThrow();

        assertThat(result.categories()).containsExactlyInAnyOrder("HORROR", "CAVES");
        assertThat(statistics.getEntityInsertCount()).isZero();
        assertThat(statistics.getEntityDeleteCount()).isZero();
        assertThat(statistics.getEntityUpdateCount()).isZero();
        assertThat(repository.findSummaryById("cc").orElseThrow().categories())
                .containsExactlyInAnyOrder("HORROR", "CAVES");
    }

    @Test
    void removeCategoryOnlyTouchesTheCategoryTable() {
        BookSummary result = repository.removeCategory("cc", "HORROR").orElseThrow();

        assertThat(result.categories()).isEmpty();
        assertThat(statistics.getEntityDeleteCount()).isZero();
        assertThat(repository.findSummaryById("cc").orElseThrow().categories()).isEmpty();
    }

    @Test
    void categoryChangesOnUnknownBookReturnEmpty() {
        assertThat(repository.addCategory("missing", "X")).isEmpty();
        assertThat(repository.removeCategory("missing", "X")).isEmpty();
    }

    private Book book(String id, String title, String author, Difficulty difficulty, Set<String> categories) {
        Section begin = new Section(1, "Start", SectionType.BEGIN, List.of(
                new Option("Walk", 2, null),
                new Option("Jump", 2, new Consequence(ConsequenceType.LOSE_HEALTH, 3, "Ouch"))));
        Section end = new Section(2, "The end", SectionType.END, List.of());

        return new Book(id, title, author, difficulty, categories, Map.of(1, begin, 2, end));
    }
}
