package com.adventurebook.adventure;

import com.adventurebook.adventure.dto.ChooseOptionRequest;
import com.adventurebook.adventure.dto.PlayResultResponse;
import com.adventurebook.adventure.dto.SectionResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books/{bookId}/sections")
public class AdventureController {

    private final AdventureService adventureService;

    public AdventureController(AdventureService adventureService) {
        this.adventureService = adventureService;
    }

    @GetMapping("/begin")
    public SectionResponse begin(@PathVariable String bookId) {
        return adventureService.begin(bookId);
    }

    @GetMapping("/{sectionId}")
    public SectionResponse read(@PathVariable String bookId, @PathVariable int sectionId) {
        return adventureService.getSection(bookId, sectionId);
    }

    @PostMapping("/{sectionId}/choose")
    public PlayResultResponse choose(@PathVariable String bookId,
                                     @PathVariable int sectionId,
                                     @Valid @RequestBody ChooseOptionRequest request) {

        return adventureService.choose(bookId, sectionId, request.optionIndex(), request.currentHealth());
    }
}
