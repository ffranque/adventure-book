package com.pictet.adventurebook.book;

import com.pictet.adventurebook.book.dto.AddCategoryRequest;
import com.pictet.adventurebook.book.dto.BookResponse;
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
    public List<BookResponse> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty
    ) {
        return bookService.search(title, author, category, difficulty);
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable String id) {
        return bookService.getById(id);
    }

    @PostMapping("/{id}/categories")
    public BookResponse addCategory(@PathVariable String id,
                                    @Valid @RequestBody AddCategoryRequest request) {

        return bookService.addCategory(id, request.category());
    }

    @DeleteMapping("/{id}/categories/{category}")
    public BookResponse removeCategory(@PathVariable String id,
                                       @PathVariable String category) {

        return bookService.removeCategory(id, category);
    }
}
