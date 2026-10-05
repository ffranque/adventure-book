package com.adventurebook.book.exception;

import com.adventurebook.common.exception.InvalidRequestException;

public class InvalidDifficultyException extends InvalidRequestException {
    
    public InvalidDifficultyException(String value) {
        super("difficulty must be one of EASY, MEDIUM, HARD but was: " + value);
    }
}
