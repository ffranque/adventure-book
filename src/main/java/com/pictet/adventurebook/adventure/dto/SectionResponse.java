package com.pictet.adventurebook.adventure.dto;

import com.pictet.adventurebook.domain.SectionType;

import java.util.List;

public record SectionResponse(int id, String text, SectionType type, List<OptionResponse> options) {
}
