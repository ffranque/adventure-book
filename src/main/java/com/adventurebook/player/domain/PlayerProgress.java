package com.adventurebook.player.domain;

public class PlayerProgress {

    private final String playerId;
    private final String bookId;
    private int currentSectionId;
    private int health;
    private ProgressStatus status;
    private final Long version;

    public PlayerProgress(String playerId, String bookId, int currentSectionId, int health, ProgressStatus status) {
        this(playerId, bookId, currentSectionId, health, status, null);
    }

    public PlayerProgress(String playerId, String bookId, int currentSectionId, int health, ProgressStatus status,
                          Long version) {
        this.playerId = playerId;
        this.bookId = bookId;
        this.currentSectionId = currentSectionId;
        this.health = health;
        this.status = status;
        this.version = version;
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

    public Long version() {
        return version;
    }
}
