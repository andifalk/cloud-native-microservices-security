package com.example.libraryserver.book.data;

import com.example.libraryserver.user.data.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class Book extends AbstractPersistable<Long> {

  @NotNull private UUID identifier;

  @NotNull
  @Pattern(regexp = "[0-9]{13}")
  private String isbn;

  @NotNull
  @Size(min = 1, max = 255)
  private String title;

  @NotNull
  @Size(min = 1, max = 2000)
  private String description;

  @NotEmpty
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> authors = new HashSet<>();

  @ManyToOne(fetch = FetchType.EAGER)
  private User borrowedByUser;

  public Book() {}

  public Book(String isbn, String title, String description, Set<String> authors) {
    this(null, isbn, title, description, authors, null);
  }

  public Book(UUID identifier, String isbn, String title, String description, Set<String> authors) {
    this(identifier, isbn, title, description, authors, null);
  }

  public Book(
      UUID identifier,
      String isbn,
      String title,
      String description,
      Set<String> authors,
      User borrowedByUser) {
    this.identifier = identifier;
    this.isbn = isbn;
    this.title = title;
    this.description = description;
    this.authors = authors;
    this.borrowedByUser = borrowedByUser;
  }

  @JsonIgnore
  @Override
  public Long getId() {
    return super.getId();
  }

  @JsonIgnore
  @Override
  public boolean isNew() {
    return super.isNew();
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

  public User getBorrowedByUser() {
    return borrowedByUser;
  }

  public void setBorrowedByUser(User borrowedbyUser) {
    this.borrowedByUser = borrowedbyUser;
  }

  @Override
  public String toString() {
    return "Book{"
        + "identifier="
        + identifier
        + ", isbn='"
        + isbn
        + '\''
        + ", title='"
        + title
        + '\''
        + ", description='"
        + description
        + '\''
        + ", authors="
        + authors
        + ", borrowedByUser="
        + borrowedByUser
        + '}';
  }
}
