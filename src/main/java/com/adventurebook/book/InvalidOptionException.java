package com.adventurebook.book;

import com.adventurebook.common.exception.InvalidRequestException;

public class InvalidOptionException extends InvalidRequestException {

    public InvalidOptionException(int optionIndex, int optionCount) {
        super("Invalid option index " + optionIndex + "; this section has " + optionCount + " option(s)");
    }
}
