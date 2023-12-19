package com.citylibrary.citylibrary.repository;

import com.citylibrary.citylibrary.model.Book;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Book.BookId> {
    Optional<Book> findById_Title(String title);
    Optional<Book> findById_Author(String author);

    @Query("select b from Book b where lower(b.id.title) like %:title%")
    List<Book> findByTitleContainingIgnoreCase(@Param("title") String title);

    @Query("select b from Book b where lower(b.id.author) like %:author%")
    List<Book> findByAuthorContainingIgnoreCase(@Param("author") String author);
}