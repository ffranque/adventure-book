package com.adventurebook.book.persistence;

import com.adventurebook.adventure.SectionRepository;
import com.adventurebook.domain.Section;
import com.adventurebook.domain.SectionType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaSectionRepository implements SectionRepository {

    private final SectionEntityRepository sectionEntityRepository;
    private final BookEntityMapper bookEntityMapper;

    JpaSectionRepository(SectionEntityRepository sectionEntityRepository, BookEntityMapper bookEntityMapper) {
        this.sectionEntityRepository = sectionEntityRepository;
        this.bookEntityMapper = bookEntityMapper;
    }

    @Override
    public Optional<Section> findSection(String bookId, int sectionId) {
        return sectionEntityRepository.findByBookIdAndSectionNumber(bookId, sectionId)
                .map(bookEntityMapper::toSectionDomain);
    }

    @Override
    public Optional<Section> findBeginSection(String bookId) {
        return sectionEntityRepository.findByBookIdAndType(bookId, SectionType.BEGIN)
                .map(bookEntityMapper::toSectionDomain);
    }
}
