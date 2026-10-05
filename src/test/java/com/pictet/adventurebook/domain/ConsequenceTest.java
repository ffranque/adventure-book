package com.pictet.adventurebook.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsequenceTest {

    @Test
    void loseHealthSubtractsValue() {
        assertThat(new Consequence(ConsequenceType.LOSE_HEALTH, 3, "ouch").applyTo(10)).isEqualTo(7);
    }

    @Test
    void gainHealthAddsValue() {
        assertThat(new Consequence(ConsequenceType.GAIN_HEALTH, 3, "phew").applyTo(5)).isEqualTo(8);
    }

    @Test
    void applyToClampsAtMinHealth() {
        assertThat(new Consequence(ConsequenceType.LOSE_HEALTH, 20, "fatal").applyTo(5))
                .isEqualTo(HealthRules.MIN_HEALTH);
    }

    @Test
    void applyToClampsAtMaxHealth() {
        assertThat(new Consequence(ConsequenceType.GAIN_HEALTH, 20, "potion").applyTo(5))
                .isEqualTo(HealthRules.MAX_HEALTH);
    }
}
