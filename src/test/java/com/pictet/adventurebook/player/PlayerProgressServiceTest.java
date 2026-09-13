package com.pictet.adventurebook.player;

import com.pictet.adventurebook.adventure.AdventureService;
import com.pictet.adventurebook.adventure.dto.PlayResultResponse;
import com.pictet.adventurebook.adventure.dto.SectionResponse;
import com.pictet.adventurebook.common.exception.adventure.AdventureAlreadyFinishedException;
import com.pictet.adventurebook.common.exception.player.PlayerProgressNotFoundException;
import com.pictet.adventurebook.domain.HealthRules;
import com.pictet.adventurebook.domain.PlayerProgress;
import com.pictet.adventurebook.domain.ProgressStatus;
import com.pictet.adventurebook.domain.SectionType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerProgressServiceTest {

    private final PlayerProgressRepository playerProgressRepository = mock(PlayerProgressRepository.class);
    private final AdventureService adventureService = mock(AdventureService.class);
    private final PlayerProgressService playerProgressService =
            new PlayerProgressService(playerProgressRepository, adventureService);

    private final SectionResponse beginSection =
            new SectionResponse(1, "You stand at the entrance.", SectionType.BEGIN, List.of());

    @Test
    void startWithNoExistingProgressCreatesNewProgressAtBeginning() {
        when(adventureService.begin("book-1")).thenReturn(beginSection);
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.empty());

        PlayResultResponse result = playerProgressService.start("alice", "book-1");

        assertThat(result.section()).isEqualTo(beginSection);
        assertThat(result.health()).isEqualTo(HealthRules.MAX_HEALTH);
        assertThat(result.dead()).isFalse();
        assertThat(result.gameOver()).isFalse();

        PlayerProgress saved = captureSavedProgress();
        assertThat(saved.playerId()).isEqualTo("alice");
        assertThat(saved.bookId()).isEqualTo("book-1");
        assertThat(saved.currentSectionId()).isEqualTo(beginSection.id());
        assertThat(saved.health()).isEqualTo(HealthRules.MAX_HEALTH);
        assertThat(saved.status()).isEqualTo(ProgressStatus.IN_PROGRESS);
    }

    @Test
    void startWithInProgressExistingRunReturnsExistingProgressWithoutSaving() {
        PlayerProgress existing = new PlayerProgress("alice", "book-1", 5, 7, ProgressStatus.IN_PROGRESS);
        when(adventureService.begin("book-1")).thenReturn(beginSection);
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.of(existing));
        SectionResponse currentSection = new SectionResponse(5, "A fork in the path.", SectionType.NODE, List.of());
        when(adventureService.getSection("book-1", 5)).thenReturn(currentSection);

        PlayResultResponse result = playerProgressService.start("alice", "book-1");

        assertThat(result.section()).isEqualTo(currentSection);
        assertThat(result.health()).isEqualTo(7);
        verify(playerProgressRepository, never()).save(existing);
    }

    @Test
    void startWithFinishedExistingRunResetsToBeginning() {
        PlayerProgress finished = new PlayerProgress("alice", "book-1", 9, 0, ProgressStatus.DEAD);
        when(adventureService.begin("book-1")).thenReturn(beginSection);
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.of(finished));

        PlayResultResponse result = playerProgressService.start("alice", "book-1");

        assertThat(result.section()).isEqualTo(beginSection);
        assertThat(result.health()).isEqualTo(HealthRules.MAX_HEALTH);

        PlayerProgress saved = captureSavedProgress();
        assertThat(saved.status()).isEqualTo(ProgressStatus.IN_PROGRESS);
        assertThat(saved.currentSectionId()).isEqualTo(beginSection.id());
        assertThat(saved.health()).isEqualTo(HealthRules.MAX_HEALTH);
    }

    @Test
    void chooseUpdatesProgressAndReturnsPlayResult() {
        PlayerProgress progress = new PlayerProgress("alice", "book-1", 1, 10, ProgressStatus.IN_PROGRESS);
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.of(progress));
        SectionResponse nextSection = new SectionResponse(2, "middle", SectionType.NODE, List.of());
        PlayResultResponse playResult = new PlayResultResponse(nextSection, 8, "You got hurt", false, false);
        when(adventureService.choose("book-1", 1, 0, 10)).thenReturn(playResult);

        PlayResultResponse result = playerProgressService.choose("alice", "book-1", 0);

        assertThat(result).isEqualTo(playResult);

        PlayerProgress saved = captureSavedProgress();
        assertThat(saved.currentSectionId()).isEqualTo(2);
        assertThat(saved.health()).isEqualTo(8);
        assertThat(saved.status()).isEqualTo(ProgressStatus.IN_PROGRESS);
    }

    @Test
    void chooseMarksProgressDeadWhenPlayResultIsDead() {
        PlayerProgress progress = new PlayerProgress("alice", "book-1", 1, 3, ProgressStatus.IN_PROGRESS);
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.of(progress));
        SectionResponse nextSection = new SectionResponse(2, "middle", SectionType.NODE, List.of());
        PlayResultResponse playResult = new PlayResultResponse(nextSection, 0, "Fatal blow", true, true);
        when(adventureService.choose("book-1", 1, 0, 3)).thenReturn(playResult);

        playerProgressService.choose("alice", "book-1", 0);

        assertThat(captureSavedProgress().status()).isEqualTo(ProgressStatus.DEAD);
    }

    @Test
    void chooseMarksProgressCompletedWhenGameOverWithoutDeath() {
        PlayerProgress progress = new PlayerProgress("alice", "book-1", 1, 10, ProgressStatus.IN_PROGRESS);
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.of(progress));
        SectionResponse endSection = new SectionResponse(2, "the end", SectionType.END, List.of());
        PlayResultResponse playResult = new PlayResultResponse(endSection, 10, null, false, true);
        when(adventureService.choose("book-1", 1, 0, 10)).thenReturn(playResult);

        playerProgressService.choose("alice", "book-1", 0);

        assertThat(captureSavedProgress().status()).isEqualTo(ProgressStatus.COMPLETED);
    }

    @Test
    void chooseWithFinishedProgressThrowsAdventureAlreadyFinishedException() {
        PlayerProgress finished = new PlayerProgress("alice", "book-1", 2, 0, ProgressStatus.DEAD);
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.of(finished));

        assertThatThrownBy(() -> playerProgressService.choose("alice", "book-1", 0))
                .isInstanceOf(AdventureAlreadyFinishedException.class)
                .hasMessageContaining("alice")
                .hasMessageContaining("book-1");
    }

    @Test
    void chooseWithNoExistingProgressThrowsPlayerProgressNotFoundException() {
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerProgressService.choose("alice", "book-1", 0))
                .isInstanceOf(PlayerProgressNotFoundException.class)
                .hasMessageContaining("alice")
                .hasMessageContaining("book-1");
    }

    @Test
    void getProgressReturnsCurrentSectionHealthAndStatusFlags() {
        PlayerProgress progress = new PlayerProgress("alice", "book-1", 5, 6, ProgressStatus.IN_PROGRESS);
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.of(progress));
        SectionResponse currentSection = new SectionResponse(5, "A fork in the path.", SectionType.NODE, List.of());
        when(adventureService.getSection("book-1", 5)).thenReturn(currentSection);

        PlayResultResponse result = playerProgressService.getProgress("alice", "book-1");

        assertThat(result.section()).isEqualTo(currentSection);
        assertThat(result.health()).isEqualTo(6);
        assertThat(result.consequenceText()).isNull();
        assertThat(result.dead()).isFalse();
        assertThat(result.gameOver()).isFalse();
    }

    @Test
    void getProgressWithDeadStatusReturnsDeadAndGameOverFlags() {
        PlayerProgress progress = new PlayerProgress("alice", "book-1", 9, 0, ProgressStatus.DEAD);
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.of(progress));
        SectionResponse currentSection = new SectionResponse(9, "you died", SectionType.NODE, List.of());
        when(adventureService.getSection("book-1", 9)).thenReturn(currentSection);

        PlayResultResponse result = playerProgressService.getProgress("alice", "book-1");

        assertThat(result.dead()).isTrue();
        assertThat(result.gameOver()).isTrue();
    }

    @Test
    void getProgressWithNoExistingProgressThrowsPlayerProgressNotFoundException() {
        when(playerProgressRepository.findByPlayerIdAndBookId("alice", "book-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerProgressService.getProgress("alice", "book-1"))
                .isInstanceOf(PlayerProgressNotFoundException.class)
                .hasMessageContaining("alice")
                .hasMessageContaining("book-1");
    }

    private PlayerProgress captureSavedProgress() {
        ArgumentCaptor<PlayerProgress> captor = ArgumentCaptor.forClass(PlayerProgress.class);
        verify(playerProgressRepository).save(captor.capture());
        return captor.getValue();
    }
}
