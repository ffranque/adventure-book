package com.pictet.adventurebook.player.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerProgressEntityRepository extends JpaRepository<PlayerProgressEntity, Long> {

    Optional<PlayerProgressEntity> findByPlayerIdAndBookId(String playerId, String bookId);
}
