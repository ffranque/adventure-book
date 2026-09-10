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

    private static final int MAX_HEALTH = 10;
    private static final int MIN_HEALTH = 0;
    private final BookRepository bookRepository;

    private static final Map<ConsequenceType, IntBinaryOperator> CONSEQUENCE_HANDLERS = Map.of(
            ConsequenceType.LOSE_HEALTH, (health, value) -> health - value,
            ConsequenceType.GAIN_HEALTH, Integer::sum
    );

    public AdventureService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public SectionResponse getBeginning(String bookId) {
        Book book = findBookOrThrow(bookId);
        return toSectionResponse(findBeginSection(book));
    }

    public SectionResponse getSection(String bookId, int sectionId) {
        Book book = findBookOrThrow(bookId);
        return toSectionResponse(findSectionOrThrow(book, sectionId));
    }

    public PlayResultResponse choose(String bookId, int sectionId, int optionIndex, int currentHealth) {
        Book book = findBookOrThrow(bookId);
        Section currentSection = findSectionOrThrow(book, sectionId);

        if (optionIndex >= currentSection.options().size()) {
            throw new InvalidOptionException(optionIndex, currentSection.options().size());
        }

        Option chosenOption = currentSection.options().get(optionIndex);
        Section nextSection = findSectionOrThrow(book, chosenOption.gotoId());

        int newHealth = currentHealth;
        String consequenceText = null;

        if (chosenOption.consequence() != null) {
            newHealth = applyConsequence(chosenOption.consequence(), currentHealth);
            consequenceText = chosenOption.consequence().text();
        }

        boolean dead = newHealth <= MIN_HEALTH;
        boolean gameOver = dead || nextSection.type() == SectionType.END;

        return new PlayResultResponse(toSectionResponse(nextSection), newHealth, consequenceText, dead, gameOver);
    }

    private Book findBookOrThrow(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    private Section findSectionOrThrow(Book book, int sectionId) {
        Section section = book.getSections().get(sectionId);
        if (section == null) {
            throw new SectionNotFoundException(book.getId(), sectionId);
        }

        return section;
    }

    private Section findBeginSection(Book book) {
        return book.getSections().values().stream()
                .filter(s -> s.type() == SectionType.BEGIN)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Book " + book.getId() + " has no BEGIN section — should be impossible, "
                                + "BookValidator should have rejected it at load time"));
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
        return Math.max(MIN_HEALTH, Math.min(MAX_HEALTH, health));
        //Math.clamp(health, 0, 10);
    }
}
