package com.pictet.adventurebook.common.exception;

public class InvalidDifficultyException extends RuntimeException {
    
    public InvalidDifficultyException(String value) {
        super("difficulty must be one of EASY, MEDIUM, HARD but was: " + value);
    }
}
