package com.adventurebook.adventure.dto;

public record PlayResultResponse(SectionResponse section,
                                 int health,
                                 String consequenceText,
                                 boolean dead,
                                 boolean gameOver) {
}
