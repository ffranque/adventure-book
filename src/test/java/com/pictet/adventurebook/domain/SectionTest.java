package com.pictet.adventurebook.domain;

import com.pictet.adventurebook.common.exception.adventure.InvalidOptionException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SectionTest {

    private final Option left = new Option("Left", 2, null);
    private final Option right = new Option("Right", 3, null);
    private final Section section = new Section(1, "fork", SectionType.NODE, List.of(left, right));

    @Test
    void optionReturnsOptionAtIndex() {
        assertThat(section.option(1)).isEqualTo(right);
    }

    @Test
    void optionWithIndexTooHighThrowsInvalidOptionException() {
        assertThatThrownBy(() -> section.option(2))
                .isInstanceOf(InvalidOptionException.class)
                .hasMessageContaining("2");
    }

    @Test
    void optionWithNegativeIndexThrowsInvalidOptionException() {
        assertThatThrownBy(() -> section.option(-1))
                .isInstanceOf(InvalidOptionException.class);
    }
}
