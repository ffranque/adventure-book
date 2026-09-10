package com.pictet.adventurebook.book;

import com.pictet.adventurebook.book.dto.AddCategoryRequest;
import com.pictet.adventurebook.book.dto.BookDetailResponse;
import com.pictet.adventurebook.book.dto.BookSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookSummaryResponse> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty
    ) {
        return bookService.search(title, author, category, difficulty);
    }

    @GetMapping("/{id}")
    public BookDetailResponse getById(@PathVariable String id) {
        return bookService.getById(id);
    }

    @PostMapping("/{id}/categories")
    public BookDetailResponse addCategory(@PathVariable String id,
                                          @Valid @RequestBody AddCategoryRequest request) {
        return bookService.addCategory(id, request.category());
    }

    @DeleteMapping("/{id}/categories/{category}")
    public BookDetailResponse removeCategory(@PathVariable String id,
                                             @PathVariable String category) {
        return bookService.removeCategory(id, category);
    }
}
