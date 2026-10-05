package com.adventurebook.book.dto;

import com.adventurebook.domain.Difficulty;

import java.util.Set;

public record BookResponse(String id,
                           String title,
                           String author,
                           Difficulty difficulty,
                           Set<String> categories) {

}
