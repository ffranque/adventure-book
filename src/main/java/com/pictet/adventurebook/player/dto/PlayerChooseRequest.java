package com.pictet.adventurebook.player.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PlayerChooseRequest(@NotNull(message = "optionIndex must not be null")
                                  @PositiveOrZero(message = "optionIndex must be zero or positive")
                                  Integer optionIndex) {
}
