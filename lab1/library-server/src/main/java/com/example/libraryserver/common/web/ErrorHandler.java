package com.example.libraryserver.common.web;

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
    return ResponseEntity.badRequest().body(ex.getBindingResult().getAllErrors().get(0).toString());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handle(RuntimeException ex) {
    LOGGER.error(ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
  }
}
