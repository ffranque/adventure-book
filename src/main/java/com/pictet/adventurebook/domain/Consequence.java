package com.pictet.adventurebook.domain;

public record Consequence(ConsequenceType type, int value, String text) {

    public int applyTo(int health) {
        return Math.clamp(type.apply(health, value), HealthRules.MIN_HEALTH, HealthRules.MAX_HEALTH);
    }
}
