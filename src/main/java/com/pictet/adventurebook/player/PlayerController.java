package com.pictet.adventurebook.player;

import com.pictet.adventurebook.adventure.dto.PlayResultResponse;
import com.pictet.adventurebook.player.dto.PlayerChooseRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players/{playerId}/books/{bookId}")
public class PlayerController {

    private final PlayerProgressService playerProgressService;

    public PlayerController(PlayerProgressService playerProgressService) {
        this.playerProgressService = playerProgressService;
    }

    @PostMapping("/start")
    public PlayResultResponse start(@PathVariable String playerId, @PathVariable String bookId) {
        return playerProgressService.start(playerId, bookId);
    }

    @PostMapping("/choose")
    public PlayResultResponse choose(@PathVariable String playerId, @PathVariable String bookId,
                                     @Valid @RequestBody PlayerChooseRequest request) {

        return playerProgressService.choose(playerId, bookId, request.optionIndex());
    }

    @GetMapping("/progress")
    public PlayResultResponse progress(@PathVariable String playerId, @PathVariable String bookId) {
        return playerProgressService.getProgress(playerId, bookId);
    }
}
