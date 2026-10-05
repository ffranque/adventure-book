package com.adventurebook.adventure;

import com.adventurebook.domain.Section;

import java.util.Optional;

public interface SectionRepository {

    Optional<Section> findSection(String bookId, int sectionId);

    Optional<Section> findBeginSection(String bookId);
}
