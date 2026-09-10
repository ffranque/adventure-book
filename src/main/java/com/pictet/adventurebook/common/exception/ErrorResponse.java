package com.pictet.adventurebook.common.exception;

import java.util.List;

public record ErrorResponse(String message, List<String> details) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, List.of());
    }

    public static ErrorResponse of(String message, List<String> details) {
        return new ErrorResponse(message, details);
    }
}
