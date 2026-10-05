package com.adventurebook.player.exception;

import com.adventurebook.common.exception.ConflictException;
import com.adventurebook.player.domain.ProgressStatus;

public class AdventureAlreadyFinishedException extends ConflictException {

    public AdventureAlreadyFinishedException(String playerId, String bookId, ProgressStatus status) {
        super("Adventure for player " + playerId + " on book " + bookId + " is already "
                + status + " — start a new one to play again");
    }
}
