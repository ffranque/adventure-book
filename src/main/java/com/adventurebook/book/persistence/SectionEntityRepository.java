package com.adventurebook.book.persistence;

import com.adventurebook.book.SectionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SectionEntityRepository extends JpaRepository<SectionEntity, Long> {

    @EntityGraph(attributePaths = "options")
    Optional<SectionEntity> findByBookIdAndSectionNumber(String bookId, int sectionNumber);

    @EntityGraph(attributePaths = "options")
    Optional<SectionEntity> findByBookIdAndType(String bookId, SectionType type);
}
