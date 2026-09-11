package com.pictet.adventurebook.player;

import com.pictet.adventurebook.domain.ProgressStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "player_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"playerId", "bookId"}))
public class PlayerProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String playerId;

    @Column(nullable = false)
    private String bookId;

    @Column(nullable = false)
    private int currentSectionId;

    @Column(nullable = false)
    private int health;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus status;

    protected PlayerProgress() {
    }

    public PlayerProgress(String playerId, String bookId, int currentSectionId, int health, ProgressStatus status) {
        this.playerId = playerId;
        this.bookId = bookId;
        this.currentSectionId = currentSectionId;
        this.health = health;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getBookId() {
        return bookId;
    }

    public int getCurrentSectionId() {
        return currentSectionId;
    }

    public int getHealth() {
        return health;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setCurrentSectionId(int currentSectionId) {
        this.currentSectionId = currentSectionId;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
    }
}
