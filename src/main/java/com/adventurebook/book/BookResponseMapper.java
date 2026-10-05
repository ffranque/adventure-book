package com.adventurebook.book;

import com.adventurebook.book.dto.BookResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface BookResponseMapper {

    BookResponse toBookResponse(BookSummary book);
}
