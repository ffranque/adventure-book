package com.pictet.adventurebook.adventure.dto;

public record PlayResultResponse(SectionResponse section,
                                 int health,
                                 String consequenceText,
                                 boolean dead,
                                 boolean gameOver) {
}
