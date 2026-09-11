package com.pictet.adventurebook.adventure.dto;

import com.pictet.adventurebook.domain.HealthRules;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ChooseOptionRequest(@NotNull(message = "optionIndex must not be null")
                                  @PositiveOrZero(message = "optionIndex must be zero or positive")
                                  Integer optionIndex,

                                  @NotNull(message = "currentHealth must not be null")
                                  @Min(value = 1, message = "currentHealth must be at least 1 — a player at 0 health is already dead")
                                  @Max(value = HealthRules.MAX_HEALTH, message = "currentHealth cannot exceed 10")
                                  Integer currentHealth) {
}
