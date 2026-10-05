package com.adventurebook.common.exception.player;

public class ConcurrentProgressUpdateException extends RuntimeException {

    public ConcurrentProgressUpdateException(String playerId, String bookId, Throwable cause) {
        super("Progress for player " + playerId + " on book " + bookId
                + " was modified by another request — fetch the progress and retry", cause);
    }
}
