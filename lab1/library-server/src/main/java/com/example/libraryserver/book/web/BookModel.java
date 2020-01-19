package com.example.libraryserver.book.web;

import com.example.libraryserver.user.data.User;
import com.example.libraryserver.user.web.UserModel;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class BookModel extends RepresentationModel<BookModel> {

  private UUID identifier;

  @NotNull
  @Pattern(regexp = "[0-9]{13}")
  private String isbn;

  @NotNull
  @Size(min = 1, max = 255)
  private String title;

  @NotNull
  @Size(min = 1, max = 2000)
  private String description;

  @NotEmpty private Set<String> authors = new HashSet<>();

  private UserModel borrowedByUser;

  public BookModel() {}

  public BookModel(String isbn, String title, String description, Set<String> authors) {
    this(isbn, title, description, authors, null);
  }

  public BookModel(String isbn, String title, String description, Set<String> authors, UserModel borrowedByUser) {
    this(null, isbn, title, description, authors, borrowedByUser);
  }

  public BookModel(UUID identifier, String isbn, String title, String description, Set<String> authors, UserModel borrowedByUser) {
    this.identifier = identifier;
    this.isbn = isbn;
    this.title = title;
    this.description = description;
    this.authors = authors;
    this.borrowedByUser = borrowedByUser;
  }

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<String> getAuthors() {
    return authors;
  }

  public void setAuthors(Set<String> authors) {
    this.authors = authors;
  }

  public UserModel getBorrowedByUser() {
    return borrowedByUser;
  }

  public void setBorrowedByUser(UserModel borrowedByUser) {
    this.borrowedByUser = borrowedByUser;
  }

}
