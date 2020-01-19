package com.example.libraryserver.book.web;

import com.example.libraryserver.book.data.Book;
import com.example.libraryserver.book.service.BookService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/books")
@Validated
public class BookRestController {

  private final BookService bookService;
  private final BookModelAssembler bookModelAssembler;

  public BookRestController(BookService bookService, BookModelAssembler bookModelAssembler) {
    this.bookService = bookService;
    this.bookModelAssembler = bookModelAssembler;
  }

  @PostMapping
  public ResponseEntity<BookModel> createBook(
      @RequestBody @Valid BookModel bookModel, HttpServletRequest request) {
    Book book =
        bookService.save(
            new Book(
                bookModel.getIsbn(),
                bookModel.getTitle(),
                bookModel.getDescription(),
                bookModel.getAuthors()));
    URI uri =
        ServletUriComponentsBuilder.fromServletMapping(request)
            .path("/books/" + book.getIdentifier())
            .build()
            .toUri();

    return ResponseEntity.created(uri).body(bookModelAssembler.toModel(book));
  }

  @PutMapping("/{bookIdentifier}")
  public ResponseEntity<BookModel> updateBook(
      @PathVariable("bookIdentifier") UUID bookIdentifier,
      @RequestBody @Valid BookModel bookModel) {

    return bookService
        .findOneByIdentifier(bookIdentifier)
        .map(
            b -> {
              b.setAuthors(bookModel.getAuthors());
              b.setIsbn(bookModel.getIsbn());
              b.setTitle(bookModel.getTitle());
              b.setDescription(bookModel.getDescription());
              return ResponseEntity.ok(bookModelAssembler.toModel(bookService.save(b)));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{bookIdentifier}/borrow/{userIdentifier}")
  public ResponseEntity<BookModel> borrowBook(
      @PathVariable("bookIdentifier") UUID bookIdentifier,
      @PathVariable("userIdentifier") UUID userIdentifier) {

    return bookService
        .borrowForUser(bookIdentifier, userIdentifier)
        .map(b -> ResponseEntity.ok(bookModelAssembler.toModel(b)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{bookIdentifier}/return/{userIdentifier}")
  public ResponseEntity<BookModel> returnBook(
      @PathVariable("bookIdentifier") UUID bookIdentifier,
      @PathVariable("userIdentifier") UUID userIdentifier) {

    return bookService
        .returnForUser(bookIdentifier, userIdentifier)
            .map(b -> ResponseEntity.ok(bookModelAssembler.toModel(b)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<CollectionModel<BookModel>> listAllBooks() {
    CollectionModel<BookModel> bookModel = bookModelAssembler.toCollectionModel(bookService.findAll());
    bookModel.add(linkTo(BookRestController.class).withSelfRel());

    return ResponseEntity.ok(bookModel);
  }

  @GetMapping("/{bookIdentifier}")
  public ResponseEntity<BookModel> getSingleBook(@PathVariable("bookIdentifier") UUID bookIdentifier) {
    return bookService
        .findOneByIdentifier(bookIdentifier)
        .map(b -> {
          BookModel bookModel = bookModelAssembler.toModel(b);
          return ResponseEntity.ok(bookModel);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{bookIdentifier}")
  public ResponseEntity<Void> deleteSingleBook(
      @PathVariable("bookIdentifier") UUID bookIdentifier) {
    return bookService.deleteOneByIdentifier(bookIdentifier)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
