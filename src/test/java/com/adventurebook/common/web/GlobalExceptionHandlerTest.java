package com.adventurebook.common.web;

import com.adventurebook.book.dto.AddCategoryRequest;
import com.adventurebook.book.exception.BookNotFoundException;
import com.adventurebook.book.exception.InvalidDifficultyException;
import com.adventurebook.book.exception.InvalidOptionException;
import com.adventurebook.book.exception.SectionNotFoundException;
import com.adventurebook.player.exception.ConcurrentProgressUpdateException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleInvalidDifficulty_returnsBadRequestWithDifficultyInMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(
                new InvalidDifficultyException("EXTREME"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("EXTREME");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void handleUnexpected_returnsInternalServerErrorWithoutLeakingDetails() {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/books/abc-123");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new NullPointerException("some internal npe detail"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).doesNotContain("some internal npe detail");
        assertThat(response.getBody().errorId()).isNotBlank();
        assertThat(response.getBody().message()).contains(response.getBody().errorId());
    }

    @Test
    void handleBookNotFound_returnsNotFoundWithBookIdInMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new BookNotFoundException("abc-123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).contains("abc-123");
    }

    @Test
    void handleValidation_returnsBadRequestWithFieldErrorInDetails() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
                new AddCategoryRequest(""), "addCategoryRequest");
        bindingResult.addError(new FieldError(
                "addCategoryRequest", "category", "category must not be blank"));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().details()).containsExactly("category: category must not be blank");
    }

    @Test
    void handleSectionNotFound_returnsNotFoundWithSectionAndBookIdInMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new SectionNotFoundException("book-abc", 999));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).contains("999").contains("book-abc");
    }

    @Test
    void handleInvalidOption_returnsBadRequestWithOptionAndMaxInMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(new InvalidOptionException(5, 2));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("5").contains("2");
    }

    @Test
    void handleNoResource_returnsNotFoundWithMethodAndPath() {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/unknown");
        NoResourceFoundException e = new NoResourceFoundException(HttpMethod.GET, "/api/v1/unknown", "api/v1/unknown");

        ResponseEntity<ErrorResponse> response = handler.handleNoResource(e, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("No endpoint for GET /api/v1/unknown");
        assertThat(response.getBody().errorId()).isNull();
    }

    @Test
    void handleTypeMismatch_returnsBadRequestNamingTheParameter() {
        MethodArgumentTypeMismatchException e = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "sectionId", mock(MethodParameter.class), null);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("sectionId must be a number");
    }

    @Test
    void handleConcurrentProgressUpdate_returnsConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(
                new ConcurrentProgressUpdateException("alice", "book-1", null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).contains("alice").contains("book-1");
    }
}
