package com.example.libraryserver.book.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, Long> {

  Optional<Book> findOneByIdentifier(UUID identifier);

  Optional<Book> findOneByIsbn(String isbn);

  void deleteOneByIdentifier(UUID bookIdentifier);
}
