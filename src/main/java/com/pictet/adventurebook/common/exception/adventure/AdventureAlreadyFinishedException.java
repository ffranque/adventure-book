package com.pictet.adventurebook.common.exception.adventure;

import com.pictet.adventurebook.domain.ProgressStatus;

public class AdventureAlreadyFinishedException extends RuntimeException {

    public AdventureAlreadyFinishedException(String playerId, String bookId, ProgressStatus status) {
        super("Adventure for player " + playerId + " on book " + bookId + " is already "
                + status + " — start a new one to play again");
    }
}
