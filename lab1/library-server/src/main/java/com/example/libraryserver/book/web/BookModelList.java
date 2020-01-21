package com.example.libraryserver.book.web;

import org.springframework.hateoas.CollectionModel;

import java.util.Collection;

public class BookModelList extends CollectionModel<BookModel> {

  private final Collection<BookModel> books;

  public BookModelList(Collection<BookModel> books) {
    this.books = books;
  }

  public Collection<BookModel> getBooks() {
    return books;
  }
}
