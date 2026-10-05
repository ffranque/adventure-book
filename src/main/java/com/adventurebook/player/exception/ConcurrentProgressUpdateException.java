package com.adventurebook.player.exception;

import com.adventurebook.common.exception.ConflictException;

public class ConcurrentProgressUpdateException extends ConflictException {

    public ConcurrentProgressUpdateException(String playerId, String bookId, Throwable cause) {
        super("Progress for player " + playerId + " on book " + bookId
                + " was modified by another request — fetch the progress and retry", cause);
    }
}
