package com.pictet.adventurebook.common.exception.player;

public class PlayerProgressNotFoundException extends RuntimeException {

    public PlayerProgressNotFoundException(String playerId, String bookId) {
        super("No progress found for player " + playerId + " on book " + bookId + " — call start first");
    }
}
