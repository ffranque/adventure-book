package com.adventurebook.book;


import java.util.Optional;

public interface SectionRepository {

    Optional<Section> findSection(String bookId, int sectionId);

    Optional<Section> findBeginSection(String bookId);
}
