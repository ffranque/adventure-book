package com.pictet.adventurebook.common.exception;

import com.pictet.adventurebook.common.exception.adventure.AdventureAlreadyFinishedException;
import com.pictet.adventurebook.common.exception.adventure.InvalidOptionException;
import com.pictet.adventurebook.common.exception.adventure.SectionNotFoundException;
import com.pictet.adventurebook.common.exception.book.BookNotFoundException;
import com.pictet.adventurebook.common.exception.player.PlayerProgressNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidDifficultyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDifficulty(InvalidDifficultyException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(SectionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSectionNotFound(SectionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(InvalidOptionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOption(InvalidOptionException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("Validation failed", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of("An unexpected error occurred"));
    }

    @ExceptionHandler(PlayerProgressNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlayerProgressNotFound(PlayerProgressNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(AdventureAlreadyFinishedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyFinished(AdventureAlreadyFinishedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(e.getMessage()));
    }
}
