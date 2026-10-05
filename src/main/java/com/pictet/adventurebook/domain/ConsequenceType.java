package com.pictet.adventurebook.domain;

public enum ConsequenceType {

    LOSE_HEALTH {
        @Override
        public int apply(int health, int value) {
            return health - value;
        }
    },
    GAIN_HEALTH {
        @Override
        public int apply(int health, int value) {
            return health + value;
        }
    };

    public abstract int apply(int health, int value);
}
