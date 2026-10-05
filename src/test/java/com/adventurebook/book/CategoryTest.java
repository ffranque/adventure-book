package com.adventurebook.book;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    void normalizeTrimsAndUppercases() {
        assertThat(Category.normalize("  fiction ")).isEqualTo("FICTION");
    }
}
