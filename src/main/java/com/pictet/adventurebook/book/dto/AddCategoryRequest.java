package com.pictet.adventurebook.book.dto;

import jakarta.validation.constraints.NotBlank;

public record AddCategoryRequest(
        @NotBlank(message = "category must not be blank")
        String category
) {
}
