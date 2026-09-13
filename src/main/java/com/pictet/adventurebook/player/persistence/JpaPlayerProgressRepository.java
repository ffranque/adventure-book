package com.pictet.adventurebook.player.persistence;

import com.pictet.adventurebook.domain.PlayerProgress;
import com.pictet.adventurebook.player.PlayerProgressRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaPlayerProgressRepository implements PlayerProgressRepository {

    private final PlayerProgressEntityRepository playerProgressEntityRepository;
    private final PlayerProgressEntityMapper playerProgressEntityMapper;

    JpaPlayerProgressRepository(PlayerProgressEntityRepository playerProgressEntityRepository,
                                PlayerProgressEntityMapper playerProgressEntityMapper) {
        this.playerProgressEntityRepository = playerProgressEntityRepository;
        this.playerProgressEntityMapper = playerProgressEntityMapper;
    }

    @Override
    public Optional<PlayerProgress> findByPlayerIdAndBookId(String playerId, String bookId) {
        return playerProgressEntityRepository.findByPlayerIdAndBookId(playerId, bookId)
                .map(playerProgressEntityMapper::toPlayerProgressDomain);
    }

    @Override
    public PlayerProgress save(PlayerProgress progress) {
        PlayerProgressEntity entity = playerProgressEntityRepository
                .findByPlayerIdAndBookId(progress.playerId(), progress.bookId())
                .map(existing -> playerProgressEntityMapper.updatePlayerProgressEntity(existing, progress))
                .orElseGet(() -> playerProgressEntityMapper.toPlayerProgressEntity(progress));

        PlayerProgressEntity saved = playerProgressEntityRepository.save(entity);

        return playerProgressEntityMapper.toPlayerProgressDomain(saved);
    }
}
