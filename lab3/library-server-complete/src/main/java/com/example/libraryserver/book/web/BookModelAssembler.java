package com.example.libraryserver.book.web;

import com.example.libraryserver.book.data.Book;
import com.example.libraryserver.user.web.UserModelAssembler;
import org.owasp.encoder.Encode;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookModelAssembler extends RepresentationModelAssemblerSupport<Book, BookModel> {

  public BookModelAssembler() {
    super(BookRestController.class, BookModel.class);
  }

  @Override
  public BookModel toModel(Book book) {
    BookModel bookModel =
        outputEscaping(
            new BookModel(
                book.getIdentifier(),
                book.getIsbn(),
                book.getTitle(),
                book.getDescription(),
                book.getAuthors(),
                book.getBorrowedByUser() != null
                    ? new UserModelAssembler().toModel(book.getBorrowedByUser())
                    : null));
    bookModel.add(
        linkTo(methodOn(BookRestController.class).getSingleBook(bookModel.getIdentifier()))
            .withSelfRel());
    bookModel.add(
        linkTo(methodOn(BookRestController.class).borrowBook(bookModel.getIdentifier(), null))
            .withRel("borrow"));
    bookModel.add(
        linkTo(methodOn(BookRestController.class).returnBook(bookModel.getIdentifier(), null))
            .withRel("return"));

    return bookModel;
  }

  @Override
  public CollectionModel<BookModel> toCollectionModel(Iterable<? extends Book> entities) {

    List<BookModel> result = new ArrayList<>();

    for (Book entity : entities) {
      result.add(toModel(entity));
    }

    return new BookModelList(result);
  }

  private BookModel outputEscaping(BookModel input) {
    BookModel output = new BookModel();
    output.setDescription(Encode.forJavaScript(Encode.forHtml(input.getDescription())));
    output.setTitle(Encode.forJavaScript(Encode.forHtml(input.getTitle())));
    output.setIsbn(Encode.forJavaScript(Encode.forHtml(input.getIsbn())));
    output.setBorrowedByUser(input.getBorrowedByUser());
    output.setIdentifier(input.getIdentifier());
    for (String author : input.getAuthors()) {
      output.getAuthors().add(Encode.forJavaScript(Encode.forHtml(author)));
    }

    return output;
  }
}
