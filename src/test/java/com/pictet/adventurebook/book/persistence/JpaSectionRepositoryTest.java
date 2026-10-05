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

// Not transactional: lookups run outside any caller transaction, exactly as in production.
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({JpaSectionRepository.class, JpaBookRepository.class, BookEntityMapper.class})
class JpaSectionRepositoryTest {

    @Autowired
    private JpaSectionRepository repository;

    @Autowired
    private JpaBookRepository bookRepository;

    @Autowired
    private BookEntityRepository bookEntityRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;
    private String caverns;
    private String prisoner;

    @BeforeEach
    void setUp() {
        caverns = bookRepository.save("caverns.json", book("The Crystal Caverns", "Start in the caverns")).getId();
        prisoner = bookRepository.save("prisoner.json", book("The Prisoner", "Start in the cell")).getId();

        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @AfterEach
    void tearDown() {
        bookEntityRepository.deleteAll();
    }

    @Test
    void findSectionReturnsSectionWithOrderedOptionsInOneQuery() {
        Section section = repository.findSection(caverns, 1).orElseThrow();

        assertThat(section.text()).isEqualTo("Start in the caverns");
        assertThat(section.options())
                .extracting(Option::description)
                .containsExactly("Walk", "Jump");
        assertThat(section.options().get(1).consequence())
                .isEqualTo(new Consequence(ConsequenceType.LOSE_HEALTH, 3, "Ouch"));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void findSectionIsScopedToTheRequestedBook() {
        assertThat(repository.findSection(prisoner, 1).orElseThrow().text()).isEqualTo("Start in the cell");
    }

    @Test
    void findSectionWithUnknownBookOrSectionReturnsEmpty() {
        assertThat(repository.findSection(caverns, 99)).isEmpty();
        assertThat(repository.findSection("missing", 1)).isEmpty();
    }

    @Test
    void findBeginSectionReturnsTheBeginSection() {
        Section begin = repository.findBeginSection(caverns).orElseThrow();

        assertThat(begin.id()).isEqualTo(1);
        assertThat(begin.type()).isEqualTo(SectionType.BEGIN);
        assertThat(repository.findBeginSection("missing")).isEmpty();
    }

    private Book book(String title, String beginText) {
        Section begin = new Section(1, beginText, SectionType.BEGIN, List.of(
                new Option("Walk", 2, null),
                new Option("Jump", 2, new Consequence(ConsequenceType.LOSE_HEALTH, 3, "Ouch"))));
        Section end = new Section(2, "The end", SectionType.END, List.of());

        return new Book(null, title, "Author", Difficulty.EASY, Set.of(), Map.of(1, begin, 2, end));
    }
}
