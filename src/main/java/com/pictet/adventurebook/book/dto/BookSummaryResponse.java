package com.pictet.adventurebook.book.dto;

import com.pictet.adventurebook.domain.Difficulty;

import java.util.Set;

public record BookSummaryResponse(String id,
                                  String title,
                                  String author,
                                  Difficulty difficulty,
                                  Set<String> categories) {

}
