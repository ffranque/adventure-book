package com.adventurebook.player.persistence;

import com.adventurebook.common.exception.player.ConcurrentProgressUpdateException;
import com.adventurebook.domain.PlayerProgress;
import com.adventurebook.domain.ProgressStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({JpaPlayerProgressRepository.class, PlayerProgressEntityMapper.class})
class JpaPlayerProgressRepositoryTest {

    @Autowired
    private JpaPlayerProgressRepository repository;

    @Autowired
    private PlayerProgressEntityRepository entityRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("insert into book (id, source, title, author, difficulty) "
                + "values ('book-1', 'book-1.json', 'Title', 'Author', 'EASY')");
    }

    @AfterEach
    void tearDown() {
        entityRepository.deleteAll();
        jdbcTemplate.update("delete from book");
    }

    @Test
    void saveInsertsNewProgressAndUpdatesItWhenVersionMatches() {
        PlayerProgress created = repository.save(new PlayerProgress("alice", "book-1", 1, 10, ProgressStatus.IN_PROGRESS));

        PlayerProgress read = repository.findByPlayerIdAndBookId("alice", "book-1").orElseThrow();
        read.advanceTo(2, 7, ProgressStatus.IN_PROGRESS);
        PlayerProgress updated = repository.save(read);

        assertThat(updated.currentSectionId()).isEqualTo(2);
        assertThat(updated.health()).isEqualTo(7);
        assertThat(updated.version()).isGreaterThan(created.version());
    }

    @Test
    void saveRejectsStaleProgress() {
        repository.save(new PlayerProgress("alice", "book-1", 1, 10, ProgressStatus.IN_PROGRESS));
        PlayerProgress firstRequest = repository.findByPlayerIdAndBookId("alice", "book-1").orElseThrow();
        PlayerProgress secondRequest = repository.findByPlayerIdAndBookId("alice", "book-1").orElseThrow();

        firstRequest.advanceTo(2, 10, ProgressStatus.IN_PROGRESS);
        repository.save(firstRequest);

        secondRequest.advanceTo(3, 5, ProgressStatus.IN_PROGRESS);
        assertThatThrownBy(() -> repository.save(secondRequest))
                .isInstanceOf(ConcurrentProgressUpdateException.class)
                .hasMessageContaining("alice")
                .hasMessageContaining("book-1");

        assertThat(repository.findByPlayerIdAndBookId("alice", "book-1").orElseThrow().currentSectionId())
                .isEqualTo(2);
    }

    @Test
    void saveRejectsUnversionedProgressWhenRowAlreadyExists() {
        repository.save(new PlayerProgress("alice", "book-1", 1, 10, ProgressStatus.IN_PROGRESS));

        assertThatThrownBy(() -> repository.save(
                new PlayerProgress("alice", "book-1", 1, 10, ProgressStatus.IN_PROGRESS)))
                .isInstanceOf(ConcurrentProgressUpdateException.class);
    }
}
