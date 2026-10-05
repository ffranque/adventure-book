package com.pictet.adventurebook.adventure;

import com.pictet.adventurebook.adventure.dto.OptionResponse;
import com.pictet.adventurebook.adventure.dto.PlayResultResponse;
import com.pictet.adventurebook.adventure.dto.SectionResponse;
import com.pictet.adventurebook.book.BookRepository;
import com.pictet.adventurebook.common.exception.adventure.InvalidOptionException;
import com.pictet.adventurebook.common.exception.adventure.SectionNotFoundException;
import com.pictet.adventurebook.common.exception.book.BookNotFoundException;
import com.pictet.adventurebook.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

@Service
public class AdventureService {

    private final SectionRepository sectionRepository;
    private final BookRepository bookRepository;

    private static final Map<ConsequenceType, IntBinaryOperator> CONSEQUENCE_HANDLERS = Map.of(
            ConsequenceType.LOSE_HEALTH, (health, value) -> health - value,
            ConsequenceType.GAIN_HEALTH, Integer::sum
    );

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
        Section currentSection = findSectionOrThrow(bookId, sectionId);

        if (optionIndex >= currentSection.options().size()) {
            throw new InvalidOptionException(optionIndex, currentSection.options().size());
        }

        Option chosenOption = currentSection.options().get(optionIndex);
        Section nextSection = findSectionOrThrow(bookId, chosenOption.gotoId());

        int newHealth = currentHealth;
        String consequenceText = null;

        if (chosenOption.consequence() != null) {
            newHealth = applyConsequence(chosenOption.consequence(), currentHealth);
            consequenceText = chosenOption.consequence().text();
        }

        boolean dead = newHealth <= HealthRules.MIN_HEALTH;
        boolean gameOver = dead || nextSection.type() == SectionType.END;

        return new PlayResultResponse(toSectionResponse(nextSection), newHealth, consequenceText, dead, gameOver);
    }

    // The book's existence is only checked when the section lookup misses,
    // so the normal path costs a single query.
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

    private int applyConsequence(Consequence consequence, int currentHealth) {
        IntBinaryOperator operator = CONSEQUENCE_HANDLERS.get(consequence.type());
        if (operator == null) {
            throw new IllegalStateException("Unhandled consequence type: " + consequence.type());
        }
        return clampHealth(operator.applyAsInt(currentHealth, consequence.value()));
    }

    private int clampHealth(int health) {
        return Math.clamp(health, HealthRules.MIN_HEALTH, HealthRules.MAX_HEALTH);
    }
}
