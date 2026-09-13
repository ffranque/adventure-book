package com.pictet.adventurebook.player;

import com.pictet.adventurebook.adventure.dto.PlayResultResponse;
import com.pictet.adventurebook.adventure.dto.SectionResponse;
import com.pictet.adventurebook.domain.SectionType;
import com.pictet.adventurebook.player.dto.PlayerChooseRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerControllerTest {

    private final PlayerProgressService playerProgressService = mock(PlayerProgressService.class);
    private final PlayerController playerController = new PlayerController(playerProgressService);

    private final SectionResponse beginSection =
            new SectionResponse(1, "You stand at the entrance.", SectionType.BEGIN, List.of());

    @Test
    void startReturnsStartResultFromService() {
        PlayResultResponse startResult = new PlayResultResponse(beginSection, 10, null, false, false);
        when(playerProgressService.start("alice", "book-1")).thenReturn(startResult);

        PlayResultResponse result = playerController.start("alice", "book-1");

        assertThat(result).isEqualTo(startResult);
    }

    @Test
    void chooseDelegatesOptionIndexToService() {
        PlayResultResponse chooseResult = new PlayResultResponse(beginSection, 8, "You got hurt", false, false);
        when(playerProgressService.choose("alice", "book-1", 0)).thenReturn(chooseResult);

        PlayResultResponse result = playerController.choose("alice", "book-1", new PlayerChooseRequest(0));

        assertThat(result).isEqualTo(chooseResult);
        verify(playerProgressService).choose("alice", "book-1", 0);
    }

    @Test
    void progressReturnsCurrentProgressFromService() {
        PlayResultResponse progressResult = new PlayResultResponse(beginSection, 10, null, false, false);
        when(playerProgressService.getProgress("alice", "book-1")).thenReturn(progressResult);

        PlayResultResponse result = playerController.progress("alice", "book-1");

        assertThat(result).isEqualTo(progressResult);
    }
}
