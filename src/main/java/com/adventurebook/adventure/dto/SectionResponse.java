package com.adventurebook.adventure.dto;

import com.adventurebook.book.domain.SectionType;

import java.util.List;

public record SectionResponse(int id, String text, SectionType type, List<OptionResponse> options) {
}
