package com.adventurebook.book;

import com.adventurebook.book.dto.BookResponse;
import com.adventurebook.domain.BookSummary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface BookResponseMapper {

    BookResponse toBookResponse(BookSummary book);
}
