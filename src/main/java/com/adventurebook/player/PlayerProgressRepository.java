package com.adventurebook.player;


import java.util.Optional;

public interface PlayerProgressRepository {

    Optional<PlayerProgress> findByPlayerIdAndBookId(String playerId, String bookId);

    PlayerProgress save(PlayerProgress progress);
}
