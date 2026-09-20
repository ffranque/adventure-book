package com.pictet.adventurebook.domain;

public class PlayerProgress {

    private final String playerId;
    private final String bookId;
    private int currentSectionId;
    private int health;
    private ProgressStatus status;

    public PlayerProgress(String playerId, String bookId, int currentSectionId, int health, ProgressStatus status) {
        this.playerId = playerId;
        this.bookId = bookId;
        this.currentSectionId = currentSectionId;
        this.health = health;
        this.status = status;
    }

    public void advanceTo(int currentSectionId, int health, ProgressStatus status) {
        this.currentSectionId = currentSectionId;
        this.health = health;
        this.status = status;
    }

    public String playerId() {
        return playerId;
    }

    public String bookId() {
        return bookId;
    }

    public int currentSectionId() {
        return currentSectionId;
    }

    public int health() {
        return health;
    }

    public ProgressStatus status() {
        return status;
    }
}
