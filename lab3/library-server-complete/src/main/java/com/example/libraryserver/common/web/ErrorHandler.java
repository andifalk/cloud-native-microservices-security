package com.example.libraryserver.common.web;

import com.example.libraryserver.user.service.InvalidPasswordError;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice(annotations = RestController.class)
public class ErrorHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<String> handle(MethodArgumentNotValidException ex) {
    LOGGER.warn(ex.getMessage());
    StringBuilder builder = new StringBuilder();
    ex.getBindingResult().getAllErrors().forEach(e -> builder.append(e.toString()));
    return ResponseEntity.badRequest()
        .body(Encode.forJavaScriptSource(Encode.forHtmlContent(builder.toString())));
  }

  @ExceptionHandler(InvalidPasswordError.class)
  public ResponseEntity<String> handle(InvalidPasswordError ex) {
    LOGGER.warn(ex.getMessage());
    return ResponseEntity.badRequest()
        .body(Encode.forJavaScriptSource(Encode.forHtmlContent(ex.getMessage())));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handle(RuntimeException ex) {
    LOGGER.error(ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
  }
}
