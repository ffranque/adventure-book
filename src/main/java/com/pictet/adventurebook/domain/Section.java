package com.pictet.adventurebook.domain;

import java.util.List;

public record Section(int id, String text, SectionType type, List<Option> options) {

}
