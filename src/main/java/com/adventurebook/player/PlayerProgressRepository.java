package com.adventurebook.player;

import com.adventurebook.player.domain.PlayerProgress;

import java.util.Optional;

public interface PlayerProgressRepository {

    Optional<PlayerProgress> findByPlayerIdAndBookId(String playerId, String bookId);

    PlayerProgress save(PlayerProgress progress);
}
