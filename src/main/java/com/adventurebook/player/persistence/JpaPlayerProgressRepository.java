package com.adventurebook.player.persistence;

import com.adventurebook.player.ConcurrentProgressUpdateException;
import com.adventurebook.player.PlayerProgress;
import com.adventurebook.player.PlayerProgressRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public PlayerProgress save(PlayerProgress progress) {
        try {
            PlayerProgressEntity entity = playerProgressEntityRepository
                    .findByPlayerIdAndBookId(progress.playerId(), progress.bookId())
                    .map(existing -> updateIfUnchanged(existing, progress))
                    .orElseGet(() -> playerProgressEntityMapper.toPlayerProgressEntity(progress));

            PlayerProgressEntity saved = playerProgressEntityRepository.saveAndFlush(entity);

            return playerProgressEntityMapper.toPlayerProgressDomain(saved);
        } catch (OptimisticLockingFailureException | DataIntegrityViolationException e) {
            throw new ConcurrentProgressUpdateException(progress.playerId(), progress.bookId(), e);
        }
    }

    private PlayerProgressEntity updateIfUnchanged(PlayerProgressEntity existing, PlayerProgress progress) {
        if (progress.version() == null || existing.getVersion() != progress.version()) {
            throw new OptimisticLockingFailureException(
                    "Stale player progress: read version " + progress.version()
                            + " but stored version is " + existing.getVersion());
        }

        return playerProgressEntityMapper.updatePlayerProgressEntity(existing, progress);
    }
}
