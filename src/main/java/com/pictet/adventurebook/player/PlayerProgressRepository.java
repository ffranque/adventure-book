package com.pictet.adventurebook.player;

import com.pictet.adventurebook.domain.PlayerProgress;

import java.util.Optional;

public interface PlayerProgressRepository {

    Optional<PlayerProgress> findByPlayerIdAndBookId(String playerId, String bookId);

    PlayerProgress save(PlayerProgress progress);
}
