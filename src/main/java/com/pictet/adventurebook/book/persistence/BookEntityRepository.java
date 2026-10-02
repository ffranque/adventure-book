package com.pictet.adventurebook.book.persistence;

import com.pictet.adventurebook.domain.Difficulty;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookEntityRepository extends JpaRepository<BookEntity, String> {

    @EntityGraph(attributePaths = "categories")
    @Query("""
            select b from BookEntity b
            where (:title is null or locate(lower(:title), lower(b.title)) > 0)
              and (:author is null or locate(lower(:author), lower(b.author)) > 0)
              and (:category is null or :category member of b.categories)
              and (:difficulty is null or b.difficulty = :difficulty)
            order by b.title
            """)
    List<BookEntity> search(@Param("title") String title,
                            @Param("author") String author,
                            @Param("category") String category,
                            @Param("difficulty") Difficulty difficulty);

    @EntityGraph(attributePaths = "categories")
    Optional<BookEntity> findWithCategoriesById(String id);

    @EntityGraph(attributePaths = {"categories", "sections"})
    Optional<BookEntity> findWithSectionsById(String id);
}
