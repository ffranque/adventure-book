package com.pictet.adventurebook.book;

import com.pictet.adventurebook.book.dto.BookResponse;
import com.pictet.adventurebook.domain.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface BookResponseMapper {

    BookResponse toBookResponse(Book book);
}
