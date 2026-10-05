package com.adventurebook.player.exception;

import com.adventurebook.common.exception.NotFoundException;

public class PlayerProgressNotFoundException extends NotFoundException {

    public PlayerProgressNotFoundException(String playerId, String bookId) {
        super("No progress found for player " + playerId + " on book " + bookId + " — call start first");
    }
}
