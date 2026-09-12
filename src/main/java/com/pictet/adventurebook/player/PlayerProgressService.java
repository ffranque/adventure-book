package com.pictet.adventurebook.player;

import com.pictet.adventurebook.adventure.AdventureService;
import com.pictet.adventurebook.adventure.dto.PlayResultResponse;
import com.pictet.adventurebook.adventure.dto.SectionResponse;
import com.pictet.adventurebook.common.exception.adventure.AdventureAlreadyFinishedException;
import com.pictet.adventurebook.common.exception.player.PlayerProgressNotFoundException;
import com.pictet.adventurebook.domain.HealthRules;
import com.pictet.adventurebook.domain.ProgressStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerProgressService {

    private final PlayerProgressRepository playerProgressRepository;
    private final AdventureService adventureService;

    public PlayerProgressService(PlayerProgressRepository playerProgressRepository,
                                 AdventureService adventureService) {

        this.playerProgressRepository = playerProgressRepository;
        this.adventureService = adventureService;
    }

    public PlayResultResponse start(String playerId, String bookId) {
        SectionResponse beginSection = adventureService.begin(bookId);

        Optional<PlayerProgress> existingPlayer = playerProgressRepository.findByPlayerIdAndBookId(playerId, bookId);
        if (existingPlayer.isPresent() && existingPlayer.get().getStatus() == ProgressStatus.IN_PROGRESS) {
            return toPlayResultResponse(existingPlayer.get());
        }

        PlayerProgress progress = existingPlayer.orElseGet(() ->
                new PlayerProgress(playerId, bookId, beginSection.id(), HealthRules.MAX_HEALTH, ProgressStatus.IN_PROGRESS));
        progress.setCurrentSectionId(beginSection.id());
        progress.setHealth(HealthRules.MAX_HEALTH);
        progress.setStatus(ProgressStatus.IN_PROGRESS);
        playerProgressRepository.save(progress);

        return new PlayResultResponse(beginSection, HealthRules.MAX_HEALTH, null, false, false);
    }

    public PlayResultResponse choose(String playerId, String bookId, int optionIndex) {
        PlayerProgress progress = findOrThrow(playerId, bookId);
        if (progress.getStatus() != ProgressStatus.IN_PROGRESS) {
            throw new AdventureAlreadyFinishedException(playerId, bookId, progress.getStatus());
        }

        PlayResultResponse playResult = adventureService.choose(
                bookId, progress.getCurrentSectionId(), optionIndex, progress.getHealth());

        progress.setCurrentSectionId(playResult.section().id());
        progress.setHealth(playResult.health());
        progress.setStatus(deriveStatus(playResult));
        playerProgressRepository.save(progress);

        return playResult;
    }

    public PlayResultResponse getProgress(String playerId, String bookId) {
        PlayerProgress playerProgress = findOrThrow(playerId, bookId);
        return toPlayResultResponse(playerProgress);
    }

    private PlayResultResponse toPlayResultResponse(PlayerProgress progress) {
        SectionResponse section = adventureService.getSection(progress.getBookId(), progress.getCurrentSectionId());
        boolean dead = progress.getStatus() == ProgressStatus.DEAD;
        boolean gameOver = progress.getStatus() != ProgressStatus.IN_PROGRESS;
        return new PlayResultResponse(section, progress.getHealth(), null, dead, gameOver);
    }

    private ProgressStatus deriveStatus(PlayResultResponse playResult) {
        if (playResult.dead()) return ProgressStatus.DEAD;
        if (playResult.gameOver()) return ProgressStatus.COMPLETED;

        return ProgressStatus.IN_PROGRESS;
    }

    private PlayerProgress findOrThrow(String playerId, String bookId) {
        return playerProgressRepository.findByPlayerIdAndBookId(playerId, bookId)
                .orElseThrow(() -> new PlayerProgressNotFoundException(playerId, bookId));
    }
}
