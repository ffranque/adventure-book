package com.adventurebook.book;


import java.util.List;

public record Section(int id, String text, SectionType type, List<Option> options) {

    public Section {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public Option option(int index) {
        if (index < 0 || index >= options.size()) {
            throw new InvalidOptionException(index, options.size());
        }

        return options.get(index);
    }
}
