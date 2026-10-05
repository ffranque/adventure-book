package com.pictet.adventurebook.adventure;

import com.pictet.adventurebook.domain.Section;

import java.util.Optional;

public interface SectionRepository {

    Optional<Section> findSection(String bookId, int sectionId);

    Optional<Section> findBeginSection(String bookId);
}
