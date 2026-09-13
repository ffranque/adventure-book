package com.pictet.adventurebook.domain;

public record PlayerProgress(String playerId, String bookId, int currentSectionId, int health,
                             ProgressStatus status) {
}
