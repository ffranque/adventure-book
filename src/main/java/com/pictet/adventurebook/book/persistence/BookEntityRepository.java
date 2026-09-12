package com.pictet.adventurebook.book.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookEntityRepository extends JpaRepository<BookEntity, String> {

}
