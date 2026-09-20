package com.pictet.adventurebook.common.exception;

import java.util.List;

public record ErrorResponse(String message, List<String> details, String errorId) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, List.of(), null);
    }

    public static ErrorResponse of(String message, List<String> details) {
        return new ErrorResponse(message, details, null);
    }

    public static ErrorResponse ofUnexpected(String errorId) {
        return new ErrorResponse(
                "An unexpected error occurred. Please contact support with reference: " + errorId,
                List.of(),
                errorId);
    }
}
