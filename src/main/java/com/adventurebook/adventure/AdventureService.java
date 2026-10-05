package com.adventurebook.adventure;

import com.adventurebook.adventure.dto.OptionResponse;
import com.adventurebook.adventure.dto.PlayResultResponse;
import com.adventurebook.adventure.dto.SectionResponse;
import com.adventurebook.book.BookRepository;
import com.adventurebook.book.SectionRepository;
import com.adventurebook.book.domain.HealthRules;
import com.adventurebook.book.domain.Option;
import com.adventurebook.book.domain.Section;
import com.adventurebook.book.domain.SectionType;
import com.adventurebook.book.exception.BookNotFoundException;
import com.adventurebook.book.exception.SectionNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class AdventureService {

    private final SectionRepository sectionRepository;
    private final BookRepository bookRepository;

    public AdventureService(SectionRepository sectionRepository, BookRepository bookRepository) {
        this.sectionRepository = sectionRepository;
        this.bookRepository = bookRepository;
    }

    public SectionResponse begin(String bookId) {
        return toSectionResponse(findBeginSectionOrThrow(bookId));
    }

    public SectionResponse getSection(String bookId, int sectionId) {
        return toSectionResponse(findSectionOrThrow(bookId, sectionId));
    }

    public PlayResultResponse choose(String bookId, int sectionId, int optionIndex, int currentHealth) {
        Option chosenOption = findSectionOrThrow(bookId, sectionId).option(optionIndex);
        Section nextSection = findSectionOrThrow(bookId, chosenOption.gotoId());

        int newHealth = currentHealth;
        String consequenceText = null;

        if (chosenOption.consequence() != null) {
            newHealth = chosenOption.consequence().applyTo(currentHealth);
            consequenceText = chosenOption.consequence().text();
        }

        boolean dead = newHealth <= HealthRules.MIN_HEALTH;
        boolean gameOver = dead || nextSection.type() == SectionType.END;

        return new PlayResultResponse(toSectionResponse(nextSection), newHealth, consequenceText, dead, gameOver);
    }

    private Section findSectionOrThrow(String bookId, int sectionId) {
        return sectionRepository.findSection(bookId, sectionId)
                .orElseThrow(() -> {
                    requireBookExists(bookId);
                    return new SectionNotFoundException(bookId, sectionId);
                });
    }

    private Section findBeginSectionOrThrow(String bookId) {
        return sectionRepository.findBeginSection(bookId)
                .orElseThrow(() -> {
                    requireBookExists(bookId);
                    return new IllegalStateException(
                            "Book " + bookId + " has no BEGIN section — should be impossible, "
                                    + "BookValidator should have rejected it at load time");
                });
    }

    private void requireBookExists(String bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }
    }

    private SectionResponse toSectionResponse(Section section) {
        List<OptionResponse> options = IntStream.range(0, section.options().size())
                .mapToObj(i -> {
                    Option option = section.options().get(i);
                    return new OptionResponse(i, option.description(), option.gotoId());
                })
                .toList();

        return new SectionResponse(section.id(), section.text(), section.type(), options);
    }
}
