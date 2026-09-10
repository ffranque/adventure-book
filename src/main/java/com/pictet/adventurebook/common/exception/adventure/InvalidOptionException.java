package com.pictet.adventurebook.common.exception.adventure;

public class InvalidOptionException extends RuntimeException {

    public InvalidOptionException(int optionIndex, int optionCount) {
        super("Invalid option index " + optionIndex + "; this section has " + optionCount + " option(s)");
    }
}
